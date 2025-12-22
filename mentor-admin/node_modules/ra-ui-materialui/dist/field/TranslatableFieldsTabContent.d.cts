import * as React from 'react';
import { type ReactNode } from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type RaRecord } from 'ra-core';
/**
 * Default container for a group of translatable fields inside a TranslatableFields components.
 * @see TranslatableFields
 */
export declare const TranslatableFieldsTabContent: (inProps: TranslatableFieldsTabContentProps) => React.JSX.Element;
export type TranslatableFieldsTabContentProps = {
    children: ReactNode;
    className?: string;
    formGroupKeyPrefix?: string;
    groupKey: string;
    locale: string;
    record: RaRecord;
    resource?: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaTranslatableFieldsTabContent: 'root';
    }
    interface ComponentsPropsList {
        RaTranslatableFieldsTabContent: Partial<TranslatableFieldsTabContentProps>;
    }
    interface Components {
        RaTranslatableFieldsTabContent?: {
            defaultProps?: ComponentsPropsList['RaTranslatableFieldsTabContent'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaTranslatableFieldsTabContent'];
        };
    }
}
//# sourceMappingURL=TranslatableFieldsTabContent.d.ts.map