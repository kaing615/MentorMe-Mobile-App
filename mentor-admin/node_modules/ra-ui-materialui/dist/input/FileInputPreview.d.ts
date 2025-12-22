import * as React from 'react';
import { type ReactNode } from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type SvgIconProps } from '@mui/material';
export declare const FileInputPreview: (inProps: FileInputPreviewProps) => React.JSX.Element;
export interface FileInputPreviewProps {
    children: ReactNode;
    className?: string;
    onRemove: () => void;
    file: any;
    removeIcon?: React.ComponentType<SvgIconProps>;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaFileInputPreview: 'root' | 'removeButton' | 'removeIcon';
    }
    interface ComponentsPropsList {
        RaFileInputPreview: Partial<FileInputPreviewProps>;
    }
    interface Components {
        RaFileInputPreview?: {
            defaultProps?: ComponentsPropsList['RaFileInputPreview'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaFileInputPreview'];
        };
    }
}
//# sourceMappingURL=FileInputPreview.d.ts.map