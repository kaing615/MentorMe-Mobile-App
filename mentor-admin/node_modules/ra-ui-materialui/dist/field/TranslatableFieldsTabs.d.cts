import * as React from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type AppBarProps, type TabsProps } from '@mui/material';
/**
 * Default locale selector for the TranslatableFields component. Generates a tab for each specified locale.
 * @see TranslatableFields
 */
export declare const TranslatableFieldsTabs: (inProps: TranslatableFieldsTabsProps & AppBarProps) => React.JSX.Element;
export interface TranslatableFieldsTabsProps {
    TabsProps?: TabsProps;
    groupKey?: string;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaTranslatableFieldsTabs: 'root';
    }
    interface ComponentsPropsList {
        RaTranslatableFieldsTabs: Partial<TranslatableFieldsTabsProps>;
    }
    interface Components {
        RaTranslatableFieldsTabs?: {
            defaultProps?: ComponentsPropsList['RaTranslatableFieldsTabs'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaTranslatableFieldsTabs'];
        };
    }
}
//# sourceMappingURL=TranslatableFieldsTabs.d.ts.map