import * as React from 'react';
import { createContext, useContext } from 'react';
export var PreferenceKeyContext = createContext('');
export var PreferenceKeyContextProvider = function (_a) {
    var _b = _a.value, value = _b === void 0 ? '' : _b, children = _a.children;
    return (React.createElement(PreferenceKeyContext.Provider, { value: value }, children));
};
export var usePreferenceKey = function () {
    return useContext(PreferenceKeyContext);
};
//# sourceMappingURL=PreferenceKeyContext.js.map