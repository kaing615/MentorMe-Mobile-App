import { createContext, useContext } from 'react';
export var DataTableRenderContext = createContext('data');
export var useDataTableRenderContext = function () {
    return useContext(DataTableRenderContext);
};
//# sourceMappingURL=DataTableRenderContext.js.map