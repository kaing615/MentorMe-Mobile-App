import { useStore } from 'ra-core';
import { useMediaQuery } from '@mui/material';
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
export var useSidebarState = function () {
    var isXSmall = useMediaQuery(function (theme) { return theme === null || theme === void 0 ? void 0 : theme.breakpoints.down('sm'); }, { noSsr: true });
    return useStore('sidebar.open', isXSmall ? false : true);
};
//# sourceMappingURL=useSidebarState.js.map