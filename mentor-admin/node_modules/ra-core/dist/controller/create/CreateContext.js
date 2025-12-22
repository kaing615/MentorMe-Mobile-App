import { createContext } from 'react';
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
export var CreateContext = createContext(null);
CreateContext.displayName = 'CreateContext';
//# sourceMappingURL=CreateContext.js.map