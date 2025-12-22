import { createContext, useContext } from 'react';
export var DefaultTitleContext = createContext('React Admin');
export var DefaultTitleContextProvider = DefaultTitleContext.Provider;
/**
 * Get the application title defined at the `<Admin>` level
 *
 * @example
 * import { useDefaultTitle } from 'react-admin';
 *
 * const AppBar = () => {
 *    const defaultTitle = useDefaultTitle();
 *    return <span>{defaultTitle}</span>;
 * }
 */
export var useDefaultTitle = function () { return useContext(DefaultTitleContext); };
//# sourceMappingURL=DefaultTitleContext.js.map