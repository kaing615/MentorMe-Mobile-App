import * as React from 'react';
import { type ButtonProps } from '@mui/material';
import { type ComponentsOverrides } from '@mui/material/styles';
/**
 * Renders a button that lets users show / hide columns in a DataTable
 *
 * @example
 * import { ColumnsButton, DataTable } from 'react-admin';
 *
 * const PostListActions = () => (
 *   <TopToolbar>
        <ColumnsButton />
        <FilterButton />
 *   </TopToolbar>
 * );
 *
 * const PostList = () => (
 *   <List actions={<PostListActions />}>
 *     <DataTable>
 *       <DataTable.Col source="title" />
 *       <DataTable.Col source="author" />
         ...
 *     </DataTable>
 *   </List>
 * );
 */
export declare const ColumnsButton: (inProps: ColumnsButtonProps) => React.JSX.Element;
export interface ColumnsButtonProps extends ButtonProps {
    resource?: string;
    storeKey?: string;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaColumnsButton: 'root';
    }
    interface ComponentsPropsList {
        RaColumnsButton: Partial<ColumnsButtonProps>;
    }
    interface Components {
        RaColumnsButton?: {
            defaultProps?: ComponentsPropsList['RaColumnsButton'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaColumnsButton'];
        };
    }
}
//# sourceMappingURL=ColumnsButton.d.ts.map