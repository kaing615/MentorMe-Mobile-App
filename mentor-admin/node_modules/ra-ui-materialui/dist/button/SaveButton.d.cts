import * as React from 'react';
import type { UseMutationOptions } from '@tanstack/react-query';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type ButtonProps } from '@mui/material';
import { type CreateParams, type RaRecord, type TransformData, type UpdateParams } from 'ra-core';
/**
 * Submit button for resource forms (Edit and Create).
 *
 * @typedef {Object} Props the props you can use (other props are injected by the <Toolbar>)
 * @prop {string} className
 * @prop {string} label Button label. Defaults to 'ra.action.save', translated.
 * @prop {boolean} disabled Disable the button.
 * @prop {string} variant Material UI variant for the button. Defaults to 'contained'.
 * @prop {ReactNode} icon
 * @prop {function} mutationOptions Object of options passed to react-query.
 * @prop {function} transform Callback to execute before calling the dataProvider. Receives the data from the form, must return that transformed data. Can be asynchronous (and return a Promise)
 * @prop {boolean} alwaysEnable Force enabling the <SaveButton>. If it's not defined, the `<SaveButton>` will be enabled using `react-hook-form`'s `isValidating` state props and form context's `saving` prop (disabled if isValidating or saving, enabled otherwise).
 *
 * @param {Props} props
 *
 * @example // with custom success side effect
 *
 * const MySaveButton = props => {
 *     const notify = useNotify();
 *     const redirect = useRedirect();
 *     const onSuccess = (response) => {
 *         notify(`Post "${response.data.title}" saved!`);
 *         redirect('/posts');
 *     };
 *     return <SaveButton {...props} mutationOptions={{ onSuccess }} />;
 * }
 */
export declare const SaveButton: <RecordType extends RaRecord = any>(inProps: SaveButtonProps<RecordType>) => React.JSX.Element;
interface Props<RecordType extends RaRecord = any, MutationOptionsError = unknown> {
    className?: string;
    disabled?: boolean;
    icon?: React.ReactNode;
    invalid?: boolean;
    label?: string;
    mutationOptions?: UseMutationOptions<RecordType, MutationOptionsError, CreateParams<RecordType> | UpdateParams<RecordType>>;
    transform?: TransformData;
    variant?: string;
}
export type SaveButtonProps<RecordType extends RaRecord = any> = Props<RecordType> & ButtonProps & {
    alwaysEnable?: boolean;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaSaveButton: 'root';
    }
    interface ComponentsPropsList {
        RaSaveButton: Partial<SaveButtonProps>;
    }
    interface Components {
        RaSaveButton?: {
            defaultProps?: ComponentsPropsList['RaSaveButton'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaSaveButton'];
        };
    }
}
export {};
//# sourceMappingURL=SaveButton.d.ts.map