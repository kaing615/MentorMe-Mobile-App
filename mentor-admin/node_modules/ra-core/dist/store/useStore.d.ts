/**
 * Read and write a value from the Store
 *
 * useState-like hook using the global Store for persistence.
 * Each time a store value is changed, all components using this value will be re-rendered.
 *
 * @param {string} key Name of the store key. Separate with dots to namespace, e.g. 'posts.list.columns'.
 * @param {any} defaultValue Default value
 *
 * @return {Object} A value and a setter for the value, in an array - just like for useState()
 *
 * @example
 * import { useStore } from 'react-admin';
 *
 * const PostList = () => {
 *     const [density] = useStore('posts.list.density', 'small');
 *
 *     return (
 *         <List>
 *             <Datagrid size={density}>
 *                 ...
 *             </Datagrid>
 *         </List>
 *     );
 * }
 *
 * // Clicking on this button will trigger a rerender of the PostList!
 * const ChangeDensity: FC<any> = () => {
 *     const [density, setDensity] = useStore('posts.list.density', 'small');
 *
 *     const changeDensity = (): void => {
 *         setDensity(density === 'small' ? 'medium' : 'small');
 *     };
 *
 *     return (
 *         <Button onClick={changeDensity}>
 *             {`Change density (current ${density})`}
 *         </Button>
 *     );
 * };
 */
declare function useStore<T>(key: string, defaultValue: T): [T, (value: T | ((value: T) => void), defaultValue?: T) => void];
declare function useStore<T = undefined>(key: string, defaultValue?: T | undefined): [T | undefined, (value: T | ((value: T) => void), defaultValue?: T) => void];
export type useStoreResult<T = any> = [
    T,
    (value: T | ((value: T) => void), defaultValue?: T) => void
];
export { useStore };
//# sourceMappingURL=useStore.d.ts.map