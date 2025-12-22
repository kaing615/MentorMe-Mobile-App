"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ShowContext = void 0;
var react_1 = require("react");
/**
 * Context to store the result of the useShowController() hook.
 *
 * Use the useShowContext() hook to read the context. That's what the Show components do in react-admin.
 *
 * @example
 *
 * import { useShowController, ShowContextProvider } from 'ra-core';
 *
 * const Show = props => {
 *     const controllerProps = useShowController(props);
 *     return (
 *         <ShowContextProvider value={controllerProps}>
 *             ...
 *         </ShowContextProvider>
 *     );
 * };
 */
exports.ShowContext = (0, react_1.createContext)(null);
exports.ShowContext.displayName = 'ShowContext';
//# sourceMappingURL=ShowContext.js.map