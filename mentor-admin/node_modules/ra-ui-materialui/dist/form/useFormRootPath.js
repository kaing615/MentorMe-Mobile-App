import { matchPath, useLocation } from 'react-router-dom';
/**
 * This hook infers the tabbed form root path from the current location.
 */
export var useFormRootPath = function () {
    var location = useLocation();
    var createMatch = matchPath(':resource/create/*', location.pathname);
    var editMatch = matchPath(':resource/:id/*', location.pathname);
    if (createMatch) {
        return createMatch.pathnameBase;
    }
    if (editMatch) {
        return editMatch.pathnameBase;
    }
    return '';
};
//# sourceMappingURL=useFormRootPath.js.map