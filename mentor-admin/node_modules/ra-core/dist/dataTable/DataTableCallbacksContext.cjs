"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useDataTableCallbacksContext = exports.DataTableCallbacksContext = void 0;
var react_1 = require("react");
exports.DataTableCallbacksContext = (0, react_1.createContext)({});
var useDataTableCallbacksContext = function () {
    return (0, react_1.useContext)(exports.DataTableCallbacksContext);
};
exports.useDataTableCallbacksContext = useDataTableCallbacksContext;
//# sourceMappingURL=DataTableCallbacksContext.js.map