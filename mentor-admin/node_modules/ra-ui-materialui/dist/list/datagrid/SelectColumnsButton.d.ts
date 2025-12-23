import * as React from 'react';
import { type ButtonProps } from '@mui/material';
import { type ComponentsOverrides } from '@mui/material/styles';
/**
 * Renders a button that lets users show / hide columns in a configurable datagrid
 *
 * @example
 * import { SelectColumnsButton, DatagridConfigurable } from 'react-admin';
 *
 * const PostListActions = () => (
 *   <TopToolbar>
        <SelectColumnsButton />
        <FilterButton />
 *   </TopToolbar>
 * );
 *
 * const PostList = () => (
 *   <List actions={<PostListActions />}>
 *     <DatagridConfigurable>
 *       <TextField source="title" />
 *       <TextField source="author" />
         ...
 *     </DatagridConfigurable>
 *   </List>
 * );
 */
export declare const SelectColumnsButton: (inProps: SelectColumnsButtonProps) => React.JSX.Element;
export interface SelectColumnsButtonProps extends ButtonProps {
    resource?: string;
    preferenceKey?: string;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaSelectColumnsButton: 'root';
    }
    interface ComponentsPropsList {
        RaSelectColumnsButton: Partial<SelectColumnsButtonProps>;
    }
    interface Components {
        RaSelectColumnsButton?: {
            defaultProps?: ComponentsPropsList['RaSelectColumnsButton'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaSelectColumnsButton'];
        };
    }
}
//# sourceMappingURL=SelectColumnsButton.d.ts.map