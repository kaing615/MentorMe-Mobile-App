import * as React from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type TextFieldProps } from '@mui/material';
/**
 * An override of the default Material UI TextField which is resettable
 */
export declare const ResettableTextField: React.ForwardRefExoticComponent<Omit<ResettableTextFieldProps, "ref"> & React.RefAttributes<unknown>>;
interface Props {
    clearAlwaysVisible?: boolean;
    resettable?: boolean;
    readOnly?: boolean;
}
export type ResettableTextFieldProps = Props & Omit<TextFieldProps, 'onChange' | 'onPointerEnterCapture' | 'onPointerLeaveCapture'> & {
    onChange?: (eventOrValue: any) => void;
};
export declare const ResettableTextFieldClasses: {
    clearIcon: string;
    visibleClearIcon: string;
    clearButton: string;
    selectAdornment: string;
    inputAdornedEnd: string;
};
export declare const ResettableTextFieldStyles: {
    [x: string]: {
        height: number;
        width: number;
        padding?: undefined;
        position?: undefined;
        right?: undefined;
        paddingRight?: undefined;
    } | {
        width: number;
        height?: undefined;
        padding?: undefined;
        position?: undefined;
        right?: undefined;
        paddingRight?: undefined;
    } | {
        height: number;
        width: number;
        padding: number;
        position?: undefined;
        right?: undefined;
        paddingRight?: undefined;
    } | {
        position: string;
        right: number;
        height?: undefined;
        width?: undefined;
        padding?: undefined;
        paddingRight?: undefined;
    } | {
        paddingRight: number;
        height?: undefined;
        width?: undefined;
        padding?: undefined;
        position?: undefined;
        right?: undefined;
    };
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaResettableTextField: 'root' | 'clearIcon' | 'visibleClearIcon' | 'clearButton' | 'selectAdornment' | 'inputAdornedEnd';
    }
    interface ComponentsPropsList {
        RaResettableTextField: Partial<ResettableTextFieldProps>;
    }
    interface Components {
        RaResettableTextField?: {
            defaultProps?: ComponentsPropsList['RaResettableTextField'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaResettableTextField'];
        };
    }
}
export {};
//# sourceMappingURL=ResettableTextField.d.ts.map