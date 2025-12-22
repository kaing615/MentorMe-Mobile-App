import React from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type TabProps } from '@mui/material';
/**
 * Single tab that selects a locale in a TranslatableInputs component.
 * @see TranslatableInputs
 */
export declare const TranslatableInputsTab: (inProps: TranslatableInputsTabProps & TabProps) => React.JSX.Element;
export interface TranslatableInputsTabProps {
    groupKey?: string;
    locale: string;
}
export declare const TranslatableInputsTabClasses: {
    root: string;
    error: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaTranslatableInputsTab: 'root' | 'error';
    }
    interface ComponentsPropsList {
        RaTranslatableInputsTab: Partial<TranslatableInputsTabProps>;
    }
    interface Components {
        RaTranslatableInputsTab?: {
            defaultProps?: ComponentsPropsList['RaTranslatableInputsTab'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaTranslatableInputsTab'];
        };
    }
}
//# sourceMappingURL=TranslatableInputsTab.d.ts.map