"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.UserMenuContext = void 0;
var react_1 = require("react");
/**
 * This context provides access to a function for closing the user menu.
 *
 * @example
 * import { Logout, MenuItemLink, UserMenu, useUserMenu } from 'react-admin';
 *
 * const ConfigurationMenu = () => {
 *     const { onClose } = useUserMenu();
 *     return (
 *         <MenuItemLink
 *             to="/configuration"
 *             primaryText="pos.configuration"
 *             leftIcon={<SettingsIcon />}
 *             sidebarIsOpen
 *             onClick={onClose}
 *         />
 *     );
 * };
 *
 * export const MyUserMenu = () => (
 *     <UserMenu>
 *         <ConfigurationMenu />
 *         <Logout />
 *     </UserMenu>
 * );
 */
exports.UserMenuContext = (0, react_1.createContext)(undefined);
//# sourceMappingURL=UserMenuContext.js.map