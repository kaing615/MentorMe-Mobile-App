import * as React from 'react';
import { ReactElement } from 'react';
import { Exporter } from 'ra-core';
import { ToolbarProps } from '@mui/material';
/**
 * Action Toolbar for the List view
 *
 * Internal component. If you want to add or remove actions for a List view,
 * write your own ListActions Component. Then, in the <List> component,
 * use it in the `actions` prop to pass a custom component.
 *
 * @example
 * import { cloneElement } from 'react';
 * import Button from '@mui/material/Button';
 * import { TopToolbar, List, CreateButton, ExportButton } from 'react-admin';
 *
 * const PostListActions = ({ filters }) => (
 *     <TopToolbar>
 *         { cloneElement(filters, { context: 'button' }) }
 *         <CreateButton/>
 *         <ExportButton/>
 *         // Add your custom actions here //
 *         <Button onClick={customAction}>Custom Action</Button>
 *     </TopToolbar>
 * );
 *
 * export const PostList = () => (
 *     <List actions={<PostListActions />}>
 *         ...
 *     </List>
 * );
 */
export declare const ListActions: (props: ListActionsProps) => React.JSX.Element;
export interface ListActionsProps extends ToolbarProps {
    className?: string;
    resource?: string;
    filters?: ReactElement<any>;
    displayedFilters?: any;
    exporter?: Exporter | boolean;
    filterValues?: any;
    permanentFilter?: any;
    hasCreate?: boolean;
    showFilter?: (filterName: string, defaultValue: any) => void;
    total?: number;
}
//# sourceMappingURL=ListActions.d.ts.map