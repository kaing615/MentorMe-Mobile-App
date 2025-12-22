import * as React from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type CheckboxProps, type FormControlLabelProps } from '@mui/material';
import { type ChoicesProps } from 'ra-core';
export declare const CheckboxGroupInputItem: (inProps: CheckboxGroupInputItemProps) => React.JSX.Element;
export interface CheckboxGroupInputItemProps extends Omit<FormControlLabelProps, 'control' | 'label'>, Pick<ChoicesProps, 'optionValue' | 'optionText' | 'translateChoice' | 'disableValue'> {
    choice: any;
    value: any;
    fullWidth?: boolean;
    options?: CheckboxProps;
}
export declare const CheckboxGroupInputItemClasses: {
    checkbox: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaCheckboxGroupInputItem: 'root' | 'checkbox';
    }
    interface ComponentsPropsList {
        RaCheckboxGroupInputItem: Partial<CheckboxGroupInputItemProps>;
    }
    interface Components {
        RaCheckboxGroupInputItem?: {
            defaultProps?: ComponentsPropsList['RaCheckboxGroupInputItem'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaCheckboxGroupInputItem'];
        };
    }
}
//# sourceMappingURL=CheckboxGroupInputItem.d.ts.map