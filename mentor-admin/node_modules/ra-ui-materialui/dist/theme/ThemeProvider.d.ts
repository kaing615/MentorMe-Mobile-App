import * as React from 'react';
import { AdminChildren } from 'ra-core';
/**
 * This sets the Material UI theme based on the preferred theme type.
 *
 * @param props
 * @param props.children The children of the component.
 * @param {ThemeOptions} props.theme The initial theme. Optional, use the one from the context if not provided.
 *
 * @example
 *
 * import { ThemesContext, ThemeProvider } from 'react-admin';
 *
 * const App = () => (
 *    <ThemesContext.Provider value={{ lightTheme, darkTheme }}>
 *      <ThemeProvider>
 *        <Button>Test</Button>
 *      </ThemeProvider>
 *   </ThemesContext.Provider>
 * );
 */
export declare const ThemeProvider: ({ children }: ThemeProviderProps) => React.JSX.Element;
export interface ThemeProviderProps {
    children: AdminChildren;
}
//# sourceMappingURL=ThemeProvider.d.ts.map