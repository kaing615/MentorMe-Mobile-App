import { createContext, useContext } from 'react';
export var DataTableCallbacksContext = createContext({});
export var useDataTableCallbacksContext = function () {
    return useContext(DataTableCallbacksContext);
};
//# sourceMappingURL=DataTableCallbacksContext.js.map