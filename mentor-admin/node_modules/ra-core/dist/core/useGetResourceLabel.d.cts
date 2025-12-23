/**
 * A hook which returns function to get a translated resource name. It will use the label option of the `Resource` component if it was provided.
 *
 * @returns {GetResourceLabel} A function which takes a resource name and an optional number indicating the number of items (used for pluralization) and returns a translated string.
 * @example
 * const Menu = () => {
 *     const resources = useResourceDefinitions();
 *     const getResourceLabel = useGetResourceLabel();
 *
 *     return (
 *         <ul>
 *             {Object.keys(resources).map(name => (
 *                 <li key={name}>
 *                     {getResourceLabel(name, 2)}
 *                 </li>
 *             ))}
 *         </ul>
 *     )
 * }
 */
export declare const useGetResourceLabel: () => GetResourceLabel;
export type GetResourceLabel = (resource: string, count?: number) => string;
//# sourceMappingURL=useGetResourceLabel.d.ts.map