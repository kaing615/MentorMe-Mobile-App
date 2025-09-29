/**
 * Returns a callback used to call the authProvider.checkError() method
 * and an error from the dataProvider. If the authProvider rejects the call,
 * the hook logs the user out and shows a logged out notification.
 *
 * Used in the useDataProvider hook to check for access denied responses
 * (e.g. 401 or 403 responses) and trigger a logout.
 *
 * @see useLogout
 * @see useDataProvider
 *
 * @returns {Function} logoutIfAccessDenied callback
 *
 * @example
 *
 * import { useLogoutIfAccessDenied, useNotify, DataProviderContext } from 'react-admin';
 *
 * const FetchRestrictedResource = () => {
 *     const dataProvider = useContext(DataProviderContext);
 *     const logoutIfAccessDenied = useLogoutIfAccessDenied();
 *     const notify = useNotify()
 *     useEffect(() => {
 *         dataProvider.getOne('secret', { id: 123 })
 *             .catch(error => {
 *                  logoutIfAccessDenied(error);
 *                  notify('server error',  { type: 'error' });
 *              })
 *     }, []);
 *     // ...
 * }
 */
declare const useLogoutIfAccessDenied: () => LogoutIfAccessDenied;
/**
 * Call the authProvider.authError() method, using the error passed as argument.
 * If the authProvider rejects the call, logs the user out and shows a logged out notification.
 *
 * @param {Error} error An Error object (usually returned by the dataProvider)
 *
 * @return {Promise} Resolved to true if there was a logout, false otherwise
 */
type LogoutIfAccessDenied = (error?: any) => Promise<boolean>;
export default useLogoutIfAccessDenied;
//# sourceMappingURL=useLogoutIfAccessDenied.d.ts.map