import * as React from 'react';
import { type UseUpdateOptions, type RaRecord } from 'ra-core';
import { type SxProps } from '@mui/material';
export type InPlaceEditorAction = {
    type: 'edit';
} | {
    type: 'save';
    values: any;
} | {
    type: 'cancel';
} | {
    type: 'success';
} | {
    type: 'error';
    error: any;
};
export type InPlaceEditorValue = {
    state: 'editing';
} | {
    state: 'saving';
    values: any;
} | {
    state: 'reading';
};
export interface InPlaceEditorProps<RecordType extends RaRecord = any, ErrorType = Error> {
    source?: string;
    mutationMode?: 'optimistic' | 'pessimistic' | 'undoable';
    mutationOptions?: UseUpdateOptions<RecordType, ErrorType>;
    cancelOnBlur?: boolean;
    notifyOnSuccess?: boolean;
    resource?: string;
    showButtons?: boolean;
    children?: React.ReactNode;
    editor?: React.ReactNode;
    sx?: SxProps;
}
/**
 * Renders a value, and on click it turns into an editable field.
 *
 * The editable field is rendered inside a Form component, so InPlaceEditor
 * cannot be used inside another Form component.
 */
export declare const InPlaceEditor: <RecordType extends RaRecord = any, ErrorType extends Error = Error>(props: InPlaceEditorProps<RecordType, ErrorType>) => React.JSX.Element;
//# sourceMappingURL=InPlaceEditor.d.ts.map