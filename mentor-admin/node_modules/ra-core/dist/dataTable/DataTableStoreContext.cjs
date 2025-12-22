"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useDataTableStoreContext = exports.DataTableStoreContext = void 0;
var react_1 = require("react");
exports.DataTableStoreContext = (0, react_1.createContext)({
    storeKey: '',
    defaultHiddenColumns: [],
});
var useDataTableStoreContext = function () { return (0, react_1.useContext)(exports.DataTableStoreContext); };
exports.useDataTableStoreContext = useDataTableStoreContext;
//# sourceMappingURL=DataTableStoreContext.js.map