"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getMutationMode = void 0;
var getMutationMode = function (mutationMode, undoable) {
    if (mutationMode) {
        return mutationMode;
    }
    switch (undoable) {
        case true:
            return 'undoable';
        case false:
            return 'pessimistic';
        default:
            return 'undoable';
    }
};
exports.getMutationMode = getMutationMode;
//# sourceMappingURL=getMutationMode.js.map