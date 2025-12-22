"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getRelatedIds = void 0;
/**
 * Extracts, aggregates and deduplicates the ids of related records
 *
 * @example
 *     const books = [
 *         { id: 1, author_id: 123, title: 'Pride and Prejudice' },
 *         { id: 2, author_id: 123, title: 'Sense and Sensibility' },
 *         { id: 3, author_id: 456, title: 'War and Peace' },
 *     ];
 *     getRelatedIds(books, 'author_id'); => [123, 456]
 *
 * @example
 *     const books = [
 *         { id: 1, tag_ids: [1, 2], title: 'Pride and Prejudice' },
 *         { id: 2, tag_ids: [2, 3], title: 'Sense and Sensibility' },
 *         { id: 3, tag_ids: [4], title: 'War and Peace' },
 *     ];
 *     getRelatedIds(records, 'tag_ids'); => [1, 2, 3, 4]
 *
 * @param {Object[]} records An array of records
 * @param {string} field the identifier of the record field to use
 */
var getRelatedIds = function (records, field) {
    return Array.from(new Set(records
        .filter(function (record) { return record[field] != null; })
        .map(function (record) { return record[field]; })
        .reduce(function (ids, value) { return ids.concat(value); }, [])));
};
exports.getRelatedIds = getRelatedIds;
//# sourceMappingURL=getRelatedIds.js.map