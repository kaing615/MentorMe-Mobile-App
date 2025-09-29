import * as React from 'react';
/**
 * A field rendering its children.
 *
 * Designed as a wrapper for several fields, to support props read by
 * the parent element (e.g. `<SimpleShowLayout>`, `<Datagrid`).
 *
 * @example
 * import { WrapperField, TextField } from 'react-admin';
 *
 * const PostShow = () => (
 *    <Show>
 *        <SimpleShowLayout>
 *            <WrapperField label="author" sortBy="last_name">
 *               <TextField source="first_name" />
 *               <TextField source="last_name" />
 *            </WrapperField>
 *       </SimpleShowLayout>
 *   </Show>
 * );
 */
export var WrapperField = function (_a) {
    var children = _a.children;
    return React.createElement(React.Fragment, null, children);
};
WrapperField.displayName = 'WrapperField';
//# sourceMappingURL=WrapperField.js.map