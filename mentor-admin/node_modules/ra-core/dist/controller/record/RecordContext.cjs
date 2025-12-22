"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.RecordContextProvider = exports.RecordContext = void 0;
var React = __importStar(require("react"));
var react_1 = require("react");
/**
 * Context to store a record.
 *
 * @see RecordContextProvider
 * @see useRecordContext
 */
exports.RecordContext = (0, react_1.createContext)(undefined);
exports.RecordContext.displayName = 'RecordContext';
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
var RecordContextProvider = function (_a) {
    var children = _a.children, value = _a.value;
    return (React.createElement(exports.RecordContext.Provider, { value: value }, children));
};
exports.RecordContextProvider = RecordContextProvider;
//# sourceMappingURL=RecordContext.js.map