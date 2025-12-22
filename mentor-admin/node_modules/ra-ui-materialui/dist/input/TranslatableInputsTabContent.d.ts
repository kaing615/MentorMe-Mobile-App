import * as React from 'react';
import { type ReactNode } from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type StackProps } from '@mui/material';
import { type RaRecord } from 'ra-core';
/**
 * Default container for a group of translatable inputs inside a TranslatableInputs component.
 * @see TranslatableInputs
 */
export declare const TranslatableInputsTabContent: (inProps: TranslatableInputsTabContentProps) => React.JSX.Element;
export type TranslatableInputsTabContentProps<RecordType extends RaRecord | Omit<RaRecord, 'id'> = any> = StackProps & {
    children: ReactNode;
    groupKey?: string;
    locale: string;
    record?: RecordType;
    resource?: string;
};
export declare const TranslatableInputsTabContentClasses: {
    root: string;
    hidden: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaTranslatableInputsTabContent: 'root' | 'hidden';
    }
    interface ComponentsPropsList {
        RaTranslatableInputsTabContent: Partial<TranslatableInputsTabContentProps>;
    }
    interface Components {
        RaTranslatableInputsTabContent?: {
            defaultProps?: ComponentsPropsList['RaTranslatableInputsTabContent'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaTranslatableInputsTabContent'];
        };
    }
}
//# sourceMappingURL=TranslatableInputsTabContent.d.ts.map