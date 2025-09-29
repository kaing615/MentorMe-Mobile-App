import { memo } from 'react';
/**
 * A version of React.memo that preserves the original component type allowing it to accept generics.
 * See {@link https://stackoverflow.com/a/70890101}
 */
export var genericMemo = function (component) {
    var _a;
    var result = memo(component);
    // We have to set the displayName on both the field implementation and the memoized version.
    // On the implementation so that the memoized version can pick them up and users may reference the defaultProps in their components.
    // On the memoized version so that components that inspect their children props may read them.
    // @ts-ignore
    result.displayName = (_a = component.displayName) === null || _a === void 0 ? void 0 : _a.replace('Impl', '');
    return result;
};
//# sourceMappingURL=genericMemo.js.map