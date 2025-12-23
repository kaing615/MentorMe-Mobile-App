"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.populateQueryCache = void 0;
/**
 * Populate react-query's query cache with a data dictionary
 *
 * @example
 * const data = {
 *    posts: [{ id: 1, title: 'Hello, world' }, { id: 2, title: 'FooBar' }],
 *    comments: [{ id: 1, post_id: 1, body: 'Nice post!' }],
 * };
 * populateQueryCache({ data, queryClient });
 * // setQueryData(['posts', 'getOne', { id: '1' }], { id: 1, title: 'Hello, world' });
 * // setQueryData(['posts', 'getOne', { id: '2' }], { id: 2, title: 'FooBar' });
 * // setQueryData(['posts', 'getMany', { ids: ['1', '2'] }], [{ id: 1, title: 'Hello, world' }, { id: 2, title: 'FooBar' }]);
 * // setQueryData(['comments', 'getOne', { id: '1' }], { id: 1, post_id: 1, body: 'Nice post!' });
 * // setQueryData(['comments', 'getMany', { ids: ['1'] }], [{ id: 1, post_id: 1, body: 'Nice post!' });
 */
var populateQueryCache = function (_a) {
    var data = _a.data, queryClient = _a.queryClient, _b = _a.staleTime, staleTime = _b === void 0 ? 1000 : _b;
    // setQueryData doesn't accept a stale time option
    // So we set an updatedAt in the future to make sure the data isn't considered stale
    var updatedAt = Date.now() + staleTime;
    Object.keys(data).forEach(function (resource) {
        data[resource].forEach(function (record) {
            if (!record || record.id == null)
                return;
            queryClient.setQueryData([resource, 'getOne', { id: String(record.id) }], record, { updatedAt: updatedAt });
        });
        var recordIds = data[resource].map(function (record) { return String(record.id); });
        queryClient.setQueryData([resource, 'getMany', { ids: recordIds }], data[resource], { updatedAt: updatedAt });
    });
};
exports.populateQueryCache = populateQueryCache;
//# sourceMappingURL=populateQueryCache.js.map