import * as React from 'react';
import type { ReactNode } from 'react';
import { type ComponentsOverrides } from '@mui/material';
/**
 * FilterList-like component allowing to save and restore a query (filters, sort, perPage).
 *
 * Use this component in a Filter sidebar to let users store custom queries
 * that they can reuse later. The saved queries will appear as FilterListItems,
 * and can be removed.
 *
 * This component uses useStore under the hood to store saved queries in
 * localStorage, one set of saved queries per resource.
 *
 * @example
 *
 * import { FilterList, FilterListItem, List, Datagrid, SavedQueriesList } from 'react-admin';
 * import { Card, CardContent } from '@mui/material';
 *
 * const PostFilterSidebar = () => (
 *     <Card>
 *         <CardContent>
 *             <SavedQueriesList />
 *             <FilterList label="Category" icon={<CategoryIcon />}>
 *                 ...
 *             </FilterList>
 *             <FilterList label="Published" icon={<DateRangeIcon />}>
 *                ...
 *             </FilterList>
 *             <FilterList label="Popularity" icon={<DateRangeIcon />}>
 *                ...
 *             </FilterList>
 *         </CardContent>
 *     </Card>
 * );
 *
 * const PostList = () => (
 *     <List aside={<PostFilterSidebar />}>
 *         <Datagrid>
 *             ...
 *         </Datagrid>
 *     </List>
 * );
 *
 */
export declare const SavedQueriesList: (inProps: SavedQueriesListProps) => React.JSX.Element;
export declare const SavedQueriesListClasses: {
    floatingIcon: string;
    floatingTooltip: string;
    titleContainer: string;
    titleIcon: string;
};
export interface SavedQueriesListProps {
    icon?: ReactNode;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaSavedQueriesList: 'root' | 'floatingIcon' | 'floatingTooltip' | 'titleContainer' | 'titleIcon';
    }
    interface ComponentsPropsList {
        RaSavedQueriesList: Partial<SavedQueriesListProps>;
    }
    interface Components {
        RaSavedQueriesList?: {
            defaultProps?: ComponentsPropsList['RaSavedQueriesList'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaSavedQueriesList'];
        };
    }
}
//# sourceMappingURL=SavedQueriesList.d.ts.map