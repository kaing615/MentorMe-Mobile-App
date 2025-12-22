import { createContext, useContext } from 'react';
export var ReferenceFieldContext = createContext(null);
export var ReferenceFieldContextProvider = ReferenceFieldContext.Provider;
export var useReferenceFieldContext = function () {
    var context = useContext(ReferenceFieldContext);
    if (!context) {
        throw new Error('useReferenceFieldContext must be used inside a ReferenceFieldContextProvider');
    }
    return context;
};
//# sourceMappingURL=ReferenceFieldContext.js.map