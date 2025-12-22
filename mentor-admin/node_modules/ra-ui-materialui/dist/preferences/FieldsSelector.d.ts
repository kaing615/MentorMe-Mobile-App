import * as React from 'react';
/**
 * UI to select / deselect fields, and store the selection in preferences
 */
export declare const FieldsSelector: ({ name, availableName, }: {
    name?: string | undefined;
    availableName?: string | undefined;
}) => React.JSX.Element;
export interface SelectableField {
    index: string;
    source: string;
    label?: string;
}
//# sourceMappingURL=FieldsSelector.d.ts.map