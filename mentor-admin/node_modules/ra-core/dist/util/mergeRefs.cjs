"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.mergeRefs = mergeRefs;
// https://github.com/gregberge/react-merge-refs
function mergeRefs(refs) {
    return function (value) {
        refs.forEach(function (ref) {
            if (typeof ref === 'function') {
                ref(value);
            }
            else if (ref != null) {
                ref.current = value;
            }
        });
    };
}
//# sourceMappingURL=mergeRefs.js.map