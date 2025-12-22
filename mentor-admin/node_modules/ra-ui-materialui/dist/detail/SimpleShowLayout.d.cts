import * as React from 'react';
import { type ReactNode } from 'react';
import { type ComponentsOverrides, type Theme } from '@mui/material/styles';
import { type StackProps, type SxProps } from '@mui/material';
import { type RaRecord } from 'ra-core';
/**
 * Layout for a Show view showing fields in one column.
 *
 * It pulls the record from the RecordContext. It renders the record fields in
 * a single-column layout (via Material UI's `<Stack>` component).
 * `<SimpleShowLayout>` delegates the actual rendering of fields to its children.
 * It wraps each field inside a `<Labeled>` component to add a label.
 *
 * @example
 * // in src/posts.js
 * import * as React from "react";
 * import { Show, SimpleShowLayout, TextField } from 'react-admin';
 *
 * export const PostShow = () => (
 *     <Show>
 *         <SimpleShowLayout>
 *             <TextField source="title" />
 *         </SimpleShowLayout>
 *     </Show>
 * );
 *
 * // in src/App.js
 * import * as React from "react";
 * import { Admin, Resource } from 'react-admin';
 *
 * import { PostShow } from './posts';
 *
 * const App = () => (
 *     <Admin dataProvider={...}>
 *         <Resource name="posts" show={PostShow} />
 *     </Admin>
 * );
 *
 * @param {SimpleShowLayoutProps} props
 * @param {string} props.className A className to apply to the page content.
 * @param {ElementType} props.component The component to use as root component (div by default).
 * @param {ReactNode} props.divider An optional divider between each field, passed to `<Stack>`.
 * @param {number} props.spacing The spacing to use between each field, passed to `<Stack>`. Defaults to 1.
 * @param {Object} props.sx Custom style object.
 */
export declare const SimpleShowLayout: (inProps: SimpleShowLayoutProps) => React.JSX.Element | null;
export interface SimpleShowLayoutProps extends StackProps {
    children: ReactNode;
    className?: string;
    record?: RaRecord;
    sx?: SxProps<Theme>;
}
export declare const SimpleShowLayoutClasses: {
    stack: string;
    row: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaSimpleShowLayout: 'root' | 'stack' | 'row';
    }
    interface ComponentsPropsList {
        RaSimpleShowLayout: Partial<SimpleShowLayoutProps>;
    }
    interface Components {
        RaSimpleShowLayout?: {
            defaultProps?: ComponentsPropsList['RaSimpleShowLayout'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaSimpleShowLayout'];
        };
    }
}
//# sourceMappingURL=SimpleShowLayout.d.ts.map