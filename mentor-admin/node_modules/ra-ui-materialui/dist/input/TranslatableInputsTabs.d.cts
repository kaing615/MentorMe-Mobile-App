import * as React from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type AppBarProps, type TabsProps } from '@mui/material';
/**
 * Default locale selector for the TranslatableInputs component. Generates a tab for each specified locale.
 * @see TranslatableInputs
 */
export declare const TranslatableInputsTabs: (inProps: TranslatableInputsTabsProps & AppBarProps) => React.JSX.Element;
export interface TranslatableInputsTabsProps {
    groupKey?: string;
    TabsProps?: TabsProps;
}
export declare const TranslatableInputsTabsClasses: {
    root: string;
    tabs: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaTranslatableInputsTabs: 'root' | 'tabs';
    }
    interface ComponentsPropsList {
        RaTranslatableInputsTabs: Partial<TranslatableInputsTabsProps>;
    }
    interface Components {
        RaTranslatableInputsTabs?: {
            defaultProps?: ComponentsPropsList['RaTranslatableInputsTabs'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaTranslatableInputsTabs'];
        };
    }
}
//# sourceMappingURL=TranslatableInputsTabs.d.ts.map