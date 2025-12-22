"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.CreateContext = void 0;
var react_1 = require("react");
/**
 * Context to store the result of the useCreateController() hook.
 *
 * Use the useCreateContext() hook to read the context. That's what the Create components do in react-admin.
 *
 * @example
 *
 * import { useCreateController, CreateContextProvider } from 'ra-core';
 *
 * const Create = props => {
 *     const controllerProps = useCreateController(props);
 *     return (
 *         <CreateContextProvider value={controllerProps}>
 *             ...
 *         </CreateContextProvider>
 *     );
 * };
 */
exports.CreateContext = (0, react_1.createContext)(null);
exports.CreateContext.displayName = 'CreateContext';
//# sourceMappingURL=CreateContext.js.map