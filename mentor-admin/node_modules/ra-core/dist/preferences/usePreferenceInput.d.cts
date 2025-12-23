/**
 * Get the props for a preference input that changes the value on blur
 *
 * Relies on `usePreference`, so it's using the PreferenceKeyContext
 *
 * @example
 * const FontSizePreferenceInput = () => {
 *     const field = usePreferenceInput('ui.font.size', 10);
 *     return (
 *         <div>
 *             <label for="font-size">Font size</label>
 *             <input id="font-size" {...field} />
 *         </div>
 *     );
 * }
 */
export declare const usePreferenceInput: (key: string, defaultValue?: any) => {
    value: any;
    onChange: (event: any) => void;
    onBlur: () => void;
    onKeyDown: (event: any) => void;
};
//# sourceMappingURL=usePreferenceInput.d.ts.map