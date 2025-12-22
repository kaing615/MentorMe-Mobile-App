import * as React from 'react';
import { ComponentsOverrides, SxProps } from '@mui/material';
export declare const KeyboardShortcut: ({ className, keyboardShortcut, ...rest }: KeyboardShortcutProps) => React.JSX.Element | null;
export interface KeyboardShortcutProps extends React.DetailedHTMLProps<React.HTMLAttributes<HTMLDivElement>, HTMLDivElement> {
    keyboardShortcut?: string;
    sx?: SxProps;
}
declare const PREFIX = "RaKeyboardShortcut";
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        [PREFIX]: 'root' | 'kbd';
    }
    interface ComponentsPropsList {
        [PREFIX]: Partial<KeyboardShortcutProps>;
    }
    interface Components {
        [PREFIX]?: {
            defaultProps?: ComponentsPropsList[typeof PREFIX];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>[typeof PREFIX];
        };
    }
}
export {};
//# sourceMappingURL=KeyboardShortcut.d.ts.map