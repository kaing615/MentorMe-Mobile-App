import * as React from 'react';
import { createContext } from 'react';
/**
 * Context to store a record.
 *
 * @see RecordContextProvider
 * @see useRecordContext
 */
export var RecordContext = createContext(undefined);
RecordContext.displayName = 'RecordContext';
/**
 * Provider for the Record Context, to store a record.
 *
 * Use the useRecordContext() hook to read the context.
 * That's what the Edit and Show components do in react-admin.
 *
 * Many react-admin components read the RecordContext, including all Field
 * components.
 *
 * @example
 *
 * import { useGetOne, RecordContextProvider } from 'ra-core';
 *
 * const Show = ({ resource, id }) => {
 *     const { data } = useGetOne(resource, { id });
 *     return (
 *         <RecordContextProvider value={data}>
 *             ...
 *         </RecordContextProvider>
 *     );
 * };
 */
export var RecordContextProvider = function (_a) {
    var children = _a.children, value = _a.value;
    return (React.createElement(RecordContext.Provider, { value: value }, children));
};
//# sourceMappingURL=RecordContext.js.map