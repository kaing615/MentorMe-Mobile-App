import * as React from 'react';
import { type ReactElement, type ReactNode } from 'react';
import { ResponsiveStyleValue } from '@mui/system';
import { type ComponentsOverrides, type SxProps, type Theme } from '@mui/material/styles';
import { type RaRecord } from 'ra-core';
/**
 * Layout for a Show view showing fields grouped in tabs and laid out in a single column.
 *
 * It pulls the record from the RecordContext. It renders a set of `<Tabs>`,
 * each of which contains a list of record fields in a single-column layout
 * (via Material UI's `<Stack>` component).
 * `<TabbedShowLayout>` delegates the actual rendering of fields to its children,
 * which should be `<TabbedShowLayout.Tab>` components.
 * `<TabbedShowLayout.Tab>` wraps each field inside a `<Labeled>` component to add a label.
 *
 * @example
 * // in src/posts.js
 * import * as React from "react";
 * import { Show, TabbedShowLayout, TextField } from 'react-admin';
 *
 * export const PostShow = () => (
 *     <Show>
 *         <TabbedShowLayout>
 *             <TabbedShowLayout.Tab label="Content">
 *                 <TextField source="title" />
 *                 <TextField source="subtitle" />
 *            </TabbedShowLayout.Tab>
 *             <TabbedShowLayout.Tab label="Metadata">
 *                 <TextField source="category" />
 *            </TabbedShowLayout.Tab>
 *         </TabbedShowLayout>
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
 * @param {TabbedShowLayoutProps} props
 * @param {string} props.className A className to apply to the page content.
 * @param {ElementType} props.component The component to use as root component (div by default).
 * @param {ReactNode} props.divider An optional divider between each field, passed to `<Stack>`.
 * @param {number} props.spacing The spacing to use between each field, passed to `<Stack>`. Defaults to 1.
 * @param {Object} props.sx Custom style object.
 * @param {boolean} props.syncWithLocation Whether to update the URL when the tab changes. Defaults to true.
 * @param {ElementType} props.tabs A custom component for rendering tabs.
 */
export declare const TabbedShowLayout: {
    (inProps: TabbedShowLayoutProps): React.JSX.Element | null;
    Tab: (inProps: import("./Tab").TabProps) => React.JSX.Element;
};
export interface TabbedShowLayoutProps {
    children: ReactNode;
    className?: string;
    divider?: ReactNode;
    record?: RaRecord;
    rootPath?: string;
    spacing?: ResponsiveStyleValue<number | string>;
    sx?: SxProps<Theme>;
    syncWithLocation?: boolean;
    tabs?: ReactElement;
    value?: any;
}
export declare const TabbedShowLayoutClasses: {
    content: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaTabbedShowLayout: 'root' | 'content';
    }
    interface ComponentsPropsList {
        RaTabbedShowLayout: Partial<TabbedShowLayoutProps>;
    }
    interface Components {
        RaTabbedShowLayout?: {
            defaultProps?: ComponentsPropsList['RaTabbedShowLayout'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaTabbedShowLayout'];
        };
    }
}
//# sourceMappingURL=TabbedShowLayout.d.ts.map