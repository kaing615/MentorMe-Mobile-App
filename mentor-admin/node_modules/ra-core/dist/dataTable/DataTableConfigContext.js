import { createContext, useContext } from 'react';
export var DataTableConfigContext = createContext({
    expandSingle: false,
    hover: true,
    hasBulkActions: false,
});
export var useDataTableConfigContext = function () {
    return useContext(DataTableConfigContext);
};
//# sourceMappingURL=DataTableConfigContext.js.map