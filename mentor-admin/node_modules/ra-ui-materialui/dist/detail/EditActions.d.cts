import * as React from 'react';
import { ToolbarProps } from '@mui/material';
/**
 * Action Toolbar for the Edit view
 *
 * Internal component. If you want to add or remove actions for an Edit view,
 * write your own EditActions Component. Then, in the <Edit> component,
 * use it in the `actions` prop to pass a custom component.
 *
 * @example
 *     import Button from '@mui/material/Button';
 *     import { TopToolbar, EditButton, Edit } from 'react-admin';
 *
 *     const PostEditActions = () => (
 *         <TopToolbar>
 *             <EditButton />
 *             // Add your custom actions here
 *             <Button color="primary" onClick={customAction}>Custom Action</Button>
 *         </TopToolbar>
 *     );
 *
 *     export const PostEdit = (props) => (
 *         <Edit actions={<PostEditActions />} {...props}>
 *             ...
 *         </Edit>
 *     );
 */
export declare const EditActions: (props: EditActionsProps) => React.JSX.Element;
export interface EditActionsProps extends ToolbarProps {
    hasCreate?: boolean;
    hasEdit?: boolean;
    hasList?: boolean;
    hasShow?: boolean;
    resource?: string;
}
//# sourceMappingURL=EditActions.d.ts.map