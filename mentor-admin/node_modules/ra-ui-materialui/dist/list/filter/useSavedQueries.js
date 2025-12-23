import { useStore } from 'ra-core';
export var useSavedQueries = function (resource) {
    return useStore("".concat(resource, ".savedQueries"), []);
};
export var extractValidSavedQueries = function (savedQueries) {
    if (Array.isArray(savedQueries)) {
        return savedQueries.filter(function (query) { return isValidSavedQuery(query); });
    }
    return [];
};
export var areValidSavedQueries = function (savedQueries) {
    if (Array.isArray(savedQueries) &&
        savedQueries.every(function (query) { return isValidSavedQuery(query); })) {
        return true;
    }
};
export var isValidSavedQuery = function (savedQuery) {
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
//# sourceMappingURL=useSavedQueries.js.map