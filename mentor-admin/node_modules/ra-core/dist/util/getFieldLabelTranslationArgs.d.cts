interface Args {
    label?: string;
    defaultLabel?: string;
    resource?: string;
    resourceFromContext?: string;
    source?: string;
}
type TranslationArguments = [string, any?];
/**
 * Returns an array of arguments to use with the translate function for the label of a field.
 * The label will be computed from the resource and source props.
 *
 * Usage:
 *  <span>
 *      {translate(...getFieldLabelTranslationArgs({ label, resource, source }))}
 *  </span>
 *
 * @see useTranslateLabel for a ready-to-use hook
 */
export declare const getFieldLabelTranslationArgs: (options?: Args) => TranslationArguments;
export default getFieldLabelTranslationArgs;
export declare const getResourceFieldLabelKey: (resource: string, source: string) => string;
//# sourceMappingURL=getFieldLabelTranslationArgs.d.ts.map