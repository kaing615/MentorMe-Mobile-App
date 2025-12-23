import * as React from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type LinearProgressProps as ProgressProps } from '@mui/material';
/**
 * Progress bar formatted to replace an input or a field in a form layout
 *
 * Avoids visual jumps when replaced by value or form input
 *
 * @see ReferenceField
 * @see ReferenceInput
 *
 * @typedef {Object} Props the props you can use
 * @prop {Object} classes CSS class names
 * @prop {string} className CSS class applied to the LinearProgress component
 * @prop {integer} timeout Milliseconds to wait before showing the progress bar. One second by default
 *
 * @param {Props} props
 */
export declare const LinearProgress: {
    (inProps: LinearProgressProps): React.JSX.Element;
    displayName: string;
};
export interface LinearProgressProps extends ProgressProps {
    timeout?: number;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaLinearProgress: 'root';
    }
    interface ComponentsPropsList {
        RaLinearProgress: Partial<LinearProgressProps>;
    }
    interface Components {
        RaLinearProgress?: {
            defaultProps?: ComponentsPropsList['RaLinearProgress'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaLinearProgress'];
        };
    }
}
//# sourceMappingURL=LinearProgress.d.ts.map