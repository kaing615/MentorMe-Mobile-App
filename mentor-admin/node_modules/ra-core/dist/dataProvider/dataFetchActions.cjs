"use strict";
var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.sanitizeFetchType = exports.reactAdminFetchActions = exports.fetchActionsWithTotalResponse = exports.fetchActionsWithArrayOfRecordsResponse = exports.fetchActionsWithArrayOfIdentifiedRecordsResponse = exports.fetchActionsWithRecordResponse = exports.DELETE_MANY = exports.DELETE = exports.UPDATE_MANY = exports.UPDATE = exports.CREATE = exports.GET_MANY_REFERENCE = exports.GET_MANY = exports.GET_ONE = exports.GET_LIST = void 0;
exports.GET_LIST = 'GET_LIST';
exports.GET_ONE = 'GET_ONE';
exports.GET_MANY = 'GET_MANY';
exports.GET_MANY_REFERENCE = 'GET_MANY_REFERENCE';
exports.CREATE = 'CREATE';
exports.UPDATE = 'UPDATE';
exports.UPDATE_MANY = 'UPDATE_MANY';
exports.DELETE = 'DELETE';
exports.DELETE_MANY = 'DELETE_MANY';
exports.fetchActionsWithRecordResponse = ['getOne', 'create', 'update'];
exports.fetchActionsWithArrayOfIdentifiedRecordsResponse = [
    'getList',
    'getMany',
    'getManyReference',
];
exports.fetchActionsWithArrayOfRecordsResponse = __spreadArray(__spreadArray([], exports.fetchActionsWithArrayOfIdentifiedRecordsResponse, true), [
    'updateMany',
    'deleteMany',
], false);
exports.fetchActionsWithTotalResponse = ['getList', 'getManyReference'];
exports.reactAdminFetchActions = __spreadArray(__spreadArray([], exports.fetchActionsWithRecordResponse, true), exports.fetchActionsWithArrayOfRecordsResponse, true);
var sanitizeFetchType = function (fetchType) {
    switch (fetchType) {
        case exports.GET_LIST:
            return 'getList';
        case exports.GET_ONE:
            return 'getOne';
        case exports.GET_MANY:
            return 'getMany';
        case exports.GET_MANY_REFERENCE:
            return 'getManyReference';
        case exports.CREATE:
            return 'create';
        case exports.UPDATE:
            return 'update';
        case exports.UPDATE_MANY:
            return 'updateMany';
        case exports.DELETE:
            return 'delete';
        case exports.DELETE_MANY:
            return 'deleteMany';
        default:
            return fetchType;
    }
};
exports.sanitizeFetchType = sanitizeFetchType;
//# sourceMappingURL=dataFetchActions.js.map