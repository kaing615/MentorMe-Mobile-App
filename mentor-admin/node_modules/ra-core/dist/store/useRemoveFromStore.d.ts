/**
 * Get a callback to remove an item from the store
 *
 * @example
 * import { useRemoveFromStore } from 'react-admin';
 *
 * const ResetDatagridPrefs = () {
 *    const removeItem = useRemoveFromStore();
 *
 *    const handleClick = () => {
 *        removeItem('datagrid.prefs');
 *    };
 *
 *    return <Button onClick={hancleClick}>Reset datagrid preferences</Button>;
 * }
 */
export declare const useRemoveFromStore: (hookTimeKey?: string) => (key?: string) => void;
//# sourceMappingURL=useRemoveFromStore.d.ts.map