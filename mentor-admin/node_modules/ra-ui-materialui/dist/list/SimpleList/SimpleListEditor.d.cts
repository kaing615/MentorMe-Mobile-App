import * as React from 'react';
/**
 * A component which provides a configuration UI to tweak the SimpleList
 *
 * @param {SimpleListEditorProps} props
 * @param props.defaultPrimaryText The SimpleList columns
 * @param {String} props.resource The resource
 * @param {String} props.preferenceKey The key of the columns preferences
 */
export declare const SimpleListEditor: (props: SimpleListEditorProps) => React.JSX.Element;
export interface SimpleListEditorProps {
    defaultPrimaryText?: string;
    defaultSecondaryText?: string;
    defaultTertiatyText?: string;
}
//# sourceMappingURL=SimpleListEditor.d.ts.map