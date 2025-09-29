import type { QueryClient } from '@tanstack/react-query';
export type PopulateQueryCacheOptions = {
    data: Record<string, any[]>;
    queryClient: QueryClient;
    staleTime?: number;
};
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
export declare const populateQueryCache: ({ data, queryClient, staleTime, }: PopulateQueryCacheOptions) => void;
//# sourceMappingURL=populateQueryCache.d.ts.map