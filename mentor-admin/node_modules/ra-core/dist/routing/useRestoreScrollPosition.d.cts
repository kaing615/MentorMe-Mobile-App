/**
 * A hook that tracks the scroll position and restores it when the component mounts.
 * @param storeKey The key under which to store the scroll position in the store
 * @param debounceMs The debounce time in milliseconds
 *
 * @example
 * import { useRestoreScrollPosition } from 'ra-core';
 *
 * const MyCustomPage = () => {
 *   useRestoreScrollPosition('my-list');
 *
 *   return (
 *     <div>
 *       <h1>My Custom Page</h1>
 *       <VeryLongContent />
 *     </div>
 *   );
 * };
 */
export declare const useRestoreScrollPosition: (storeKey: string, debounceMs?: number) => void;
/**
 * A hook that tracks the scroll position and stores it.
 * @param storeKey The key under which to store the scroll position in the store
 * @param debounceMs The debounce time in milliseconds
 *
 * @example
 * import { useTrackScrollPosition } from 'ra-core';
 *
 * const MyCustomPage = () => {
 *   useTrackScrollPosition('my-list');
 *
 *   return (
 *     <div>
 *       <h1>My Custom Page</h1>
 *       <VeryLongContent />
 *     </div>
 *   );
 * };
 */
export declare const useTrackScrollPosition: (storeKey: string, debounceMs?: number) => [number | undefined, (value: number | undefined) => void];
//# sourceMappingURL=useRestoreScrollPosition.d.ts.map