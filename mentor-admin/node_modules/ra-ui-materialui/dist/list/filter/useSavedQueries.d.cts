import { SortPayload } from 'ra-core';
export declare const useSavedQueries: (resource: string) => [SavedQuery[], (value: SavedQuery[] | ((value: SavedQuery[]) => void), defaultValue?: SavedQuery[] | undefined) => void];
export interface SavedQuery {
    label: string;
    value: {
        filter?: any;
        displayedFilters?: any[];
        sort?: SortPayload;
        perPage?: number;
    };
}
export declare const extractValidSavedQueries: (savedQueries: SavedQuery[]) => SavedQuery[];
export declare const areValidSavedQueries: (savedQueries: SavedQuery[]) => true | undefined;
export declare const isValidSavedQuery: (savedQuery: SavedQuery) => boolean;
//# sourceMappingURL=useSavedQueries.d.ts.map