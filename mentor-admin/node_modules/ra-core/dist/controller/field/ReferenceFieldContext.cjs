"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useReferenceFieldContext = exports.ReferenceFieldContextProvider = exports.ReferenceFieldContext = void 0;
var react_1 = require("react");
exports.ReferenceFieldContext = (0, react_1.createContext)(null);
exports.ReferenceFieldContextProvider = exports.ReferenceFieldContext.Provider;
var useReferenceFieldContext = function () {
    var context = (0, react_1.useContext)(exports.ReferenceFieldContext);
    if (!context) {
        throw new Error('useReferenceFieldContext must be used inside a ReferenceFieldContextProvider');
    }
    return context;
};
exports.useReferenceFieldContext = useReferenceFieldContext;
//# sourceMappingURL=ReferenceFieldContext.js.map