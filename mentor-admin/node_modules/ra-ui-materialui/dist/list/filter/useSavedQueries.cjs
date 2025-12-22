"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.isValidSavedQuery = exports.areValidSavedQueries = exports.extractValidSavedQueries = exports.useSavedQueries = void 0;
var ra_core_1 = require("ra-core");
var useSavedQueries = function (resource) {
    return (0, ra_core_1.useStore)("".concat(resource, ".savedQueries"), []);
};
exports.useSavedQueries = useSavedQueries;
var extractValidSavedQueries = function (savedQueries) {
    if (Array.isArray(savedQueries)) {
        return savedQueries.filter(function (query) { return (0, exports.isValidSavedQuery)(query); });
    }
    return [];
};
exports.extractValidSavedQueries = extractValidSavedQueries;
var areValidSavedQueries = function (savedQueries) {
    if (Array.isArray(savedQueries) &&
        savedQueries.every(function (query) { return (0, exports.isValidSavedQuery)(query); })) {
        return true;
    }
};
exports.areValidSavedQueries = areValidSavedQueries;
var isValidSavedQuery = function (savedQuery) {
    var _a, _b;
    if (savedQuery.label &&
        typeof savedQuery.label === 'string' &&
        savedQuery.value &&
        typeof Array.isArray(savedQuery.value.displayedFilters) &&
        typeof savedQuery.value.perPage === 'number' &&
        typeof ((_a = savedQuery.value.sort) === null || _a === void 0 ? void 0 : _a.field) === 'string' &&
        typeof ((_b = savedQuery.value.sort) === null || _b === void 0 ? void 0 : _b.order) === 'string' &&
        typeof savedQuery.value.filter === 'object') {
        return true;
    }
    return false;
};
exports.isValidSavedQuery = isValidSavedQuery;
//# sourceMappingURL=useSavedQueries.js.map