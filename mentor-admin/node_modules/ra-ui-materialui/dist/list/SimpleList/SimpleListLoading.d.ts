import * as React from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type ListProps } from '@mui/material';
export declare const SimpleListLoading: (inProps: SimpleListLoadingProps) => React.JSX.Element | null;
export declare const SimpleListLoadingClasses: {
    primary: string;
    tertiary: string;
};
export interface SimpleListLoadingProps extends ListProps {
    className?: string;
    hasLeftAvatarOrIcon?: boolean;
    hasRightAvatarOrIcon?: boolean;
    hasSecondaryText?: boolean;
    hasTertiaryText?: boolean;
    nbFakeLines?: number;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaSimpleListLoading: 'root' | 'primary' | 'tertiary';
    }
    interface ComponentsPropsList {
        RaSimpleListLoading: Partial<SimpleListLoadingProps>;
    }
    interface Components {
        RaSimpleListLoading?: {
            defaultProps?: ComponentsPropsList['RaSimpleListLoading'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaSimpleListLoading'];
        };
    }
}
//# sourceMappingURL=SimpleListLoading.d.ts.map