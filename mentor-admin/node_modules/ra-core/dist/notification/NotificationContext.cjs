"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.NotificationContext = void 0;
var react_1 = require("react");
/**
 * Context for the notification state and modifiers
 *
 * @example // display notifications
 * import { useNotificationContext } from 'react-admin';
 *
 * const App = () => {
 *    const { notifications } = useNotificationContext();
 *    return (
 *        <ul>
 *            {notifications.map(({ message }) => (
 *                <li key={index}>{ message }</li>
 *            ))}
 *        </ul>
 *    );
 * };
 *
 * @example // reset notifications
 * import { useNotificationContext } from 'react-admin';
 *
 * const ResetNotificationsButton = () => {
 *    const { resetNotifications } = useNotificationContext();
 *    return (
 *        <button onClick={() => resetNotifications()}>Reset notifications</button>
 *    );
 * };
 */
exports.NotificationContext = (0, react_1.createContext)({
    notifications: [],
    addNotification: function () { },
    takeNotification: function () { },
    resetNotifications: function () { },
    setNotifications: function () { },
});
//# sourceMappingURL=NotificationContext.js.map