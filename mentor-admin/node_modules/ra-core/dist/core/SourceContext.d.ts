export type SourceContextValue = {
    getSource: (source: string) => string;
    getLabel: (source: string) => string;
};
/**
 * Context that provides a function that accept a source and return getters for the modified source and label.
 *
 * This allows some special inputs to prefix or suffix the source of their children.
 *
 * @example
 * const sourceContext = {
 *  getSource: source => `coordinates.${source}`,
 *  getLabel: source => `resources.posts.fields.${source}`,
 * }
 * const CoordinatesInput = () => {
 *   return (
 *     <SourceContextProvider value={sourceContext}>
 *       <TextInput source="lat" />
 *       <TextInput source="lng" />
 *     </SourceContextProvider>
 *   );
 * };
 */
export declare const SourceContext: import("react").Context<SourceContextValue | undefined>;
export declare const SourceContextProvider: import("react").Provider<SourceContextValue | undefined>;
export declare const useSourceContext: () => SourceContextValue;
export declare const useOptionalSourceContext: () => SourceContextValue | undefined;
//# sourceMappingURL=SourceContext.d.ts.map