import * as React from 'react';
import { type MouseEventHandler, type ComponentType } from 'react';
import { type DialogProps } from '@mui/material';
import { type ComponentsOverrides } from '@mui/material/styles';
/**
 * Confirmation dialog
 *
 * @example
 * <Confirm
 *     isOpen={true}
 *     title="Delete Item"
 *     content="Are you sure you want to delete this item?"
 *     confirm="Yes"
 *     confirmColor="primary"
 *     ConfirmIcon=ActionCheck
 *     CancelIcon=AlertError
 *     cancel="Cancel"
 *     onConfirm={() => { // do something }}
 *     onClose={() => { // do something }}
 * />
 */
export declare const Confirm: (inProps: ConfirmProps) => React.JSX.Element;
export interface ConfirmProps extends Omit<DialogProps, 'open' | 'onClose' | 'title' | 'content'> {
    cancel?: string;
    className?: string;
    confirm?: string;
    confirmColor?: 'primary' | 'warning';
    ConfirmIcon?: ComponentType;
    CancelIcon?: ComponentType;
    content: React.ReactNode;
    isOpen?: boolean;
    loading?: boolean;
    onClose: MouseEventHandler;
    onConfirm: MouseEventHandler;
    title: React.ReactNode;
    /**
     * @deprecated use `titleTranslateOptions` and `contentTranslateOptions` instead
     */
    translateOptions?: object;
    titleTranslateOptions?: object;
    contentTranslateOptions?: object;
}
export declare const ConfirmClasses: {
    confirmPrimary: string;
    confirmWarning: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaConfirm: 'root' | 'confirmPrimary' | 'confirmWarning';
    }
    interface ComponentsPropsList {
        RaConfirm: Partial<ConfirmProps>;
    }
    interface Components {
        RaConfirm?: {
            defaultProps?: ComponentsPropsList['RaConfirm'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaConfirm'];
        };
    }
}
//# sourceMappingURL=Confirm.d.ts.map