var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
import { useFormContext, useWatch } from 'react-hook-form';
// hook taken from https://react-hook-form.com/docs/usewatch/#rules
export var useFormValues = function () {
    var getValues = useFormContext().getValues;
    return __assign(__assign({}, useWatch()), getValues());
};
//# sourceMappingURL=useFormValues.js.map