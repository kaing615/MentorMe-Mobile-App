"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useDefaultTitle = exports.DefaultTitleContextProvider = exports.DefaultTitleContext = void 0;
var react_1 = require("react");
exports.DefaultTitleContext = (0, react_1.createContext)('React Admin');
exports.DefaultTitleContextProvider = exports.DefaultTitleContext.Provider;
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
var useDefaultTitle = function () { return (0, react_1.useContext)(exports.DefaultTitleContext); };
exports.useDefaultTitle = useDefaultTitle;
//# sourceMappingURL=DefaultTitleContext.js.map