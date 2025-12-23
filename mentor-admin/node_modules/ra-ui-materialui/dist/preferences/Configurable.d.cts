import * as React from 'react';
import { type ReactElement } from 'react';
import { type ComponentsOverrides, type SxProps, type Theme } from '@mui/material/styles';
/**
 * Wrap any component with this component to make it configurable
 *
 * When the edit mode is enabled, users will see a button to edit the component;
 * when clicked, the inspector will show the editor element.
 *
 * Creates a context for the preference key, so that both the child component
 * and the editor can access it using usePreferenceKey();
 *
 * @example
 * const ConfigurableTextBlock = ({ preferenceKey = "TextBlock", ...props }) => (
 *     <Configurable editor={<TextBlockInspector />} preferenceKey={preferenceKey}>
 *         <TextBlock {...props} />
 *     </Configurable>
 * );
 */
export declare const Configurable: (inProps: ConfigurableProps) => React.JSX.Element;
export interface ConfigurableProps {
    children: ReactElement;
    editor: ReactElement;
    preferenceKey: string;
    openButtonLabel?: string;
    sx?: SxProps<Theme>;
}
export declare const ConfigurableClasses: {
    root: string;
    editMode: string;
    editorActive: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaConfigurable: 'root' | 'editMode' | 'editorActive';
    }
    interface ComponentsPropsList {
        RaConfigurable: Partial<ConfigurableProps>;
    }
    interface Components {
        RaConfigurable?: {
            defaultProps?: ComponentsPropsList['RaConfigurable'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaConfigurable'];
        };
    }
}
//# sourceMappingURL=Configurable.d.ts.map