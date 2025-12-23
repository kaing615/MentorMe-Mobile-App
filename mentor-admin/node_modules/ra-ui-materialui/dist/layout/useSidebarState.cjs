"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useSidebarState = void 0;
var ra_core_1 = require("ra-core");
var material_1 = require("@mui/material");
/**
 * A hook that returns the sidebar open state and a function to toggle it.
 *
 * The sidebar is open by default on desktop, and closed by default on mobile.
 *
 * @example
 * const ToggleSidebar = () => {
 *     const [open, setOpen] = useSidebarState();
 *     return (
 *         <Button onClick={() => setOpen(!open)}>
 *             {open ? 'Open' : 'Close'}
 *         </Button>
 *     );
 * };
 */
var useSidebarState = function () {
    var isXSmall = (0, material_1.useMediaQuery)(function (theme) { return theme === null || theme === void 0 ? void 0 : theme.breakpoints.down('sm'); }, { noSsr: true });
    return (0, ra_core_1.useStore)('sidebar.open', isXSmall ? false : true);
};
exports.useSidebarState = useSidebarState;
//# sourceMappingURL=useSidebarState.js.map