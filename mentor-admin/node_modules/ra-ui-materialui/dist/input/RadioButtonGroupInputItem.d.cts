import * as React from 'react';
import { type ChoicesProps } from 'ra-core';
import { FormControlLabelProps } from '@mui/material';
import { ComponentsOverrides } from '@mui/material/styles';
export declare const RadioButtonGroupInputItem: (props: RadioButtonGroupInputItemProps) => React.JSX.Element;
export default RadioButtonGroupInputItem;
export interface RadioButtonGroupInputItemProps extends Omit<FormControlLabelProps, 'control' | 'label'>, Pick<ChoicesProps, 'optionValue' | 'optionText' | 'translateChoice' | 'disableValue'> {
    choice: any;
    source: any;
}
declare const PREFIX = "RaRadioButtonGroupInputItem";
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        [PREFIX]: 'root';
    }
    interface ComponentsPropsList {
        [PREFIX]: Partial<RadioButtonGroupInputItemProps>;
    }
    interface Components {
        [PREFIX]?: {
            defaultProps?: ComponentsPropsList[typeof PREFIX];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>[typeof PREFIX];
        };
    }
}
//# sourceMappingURL=RadioButtonGroupInputItem.d.ts.map