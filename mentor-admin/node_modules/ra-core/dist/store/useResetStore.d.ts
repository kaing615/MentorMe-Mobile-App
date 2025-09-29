/**
 * Get a callback to remove all items from the store
 *
 * @example
 * import { useResetStore } from 'react-admin';
 *
 * const ResetPrefs = () {
 *    const reset = useResetStore();
 *
 *    const handleClick = () => {
 *        reset();
 *    };
 *
 *    return <Button onClick={handleClick}>Reset preferences</Button>;
 * }
 */
export declare const useResetStore: () => () => void;
//# sourceMappingURL=useResetStore.d.ts.map