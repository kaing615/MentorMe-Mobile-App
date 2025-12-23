/**
 * Get a preference value from the store
 *
 * Relies on the store, using a key namespaced with the preference key from the PreferenceKeyContext
 * @example
 *
 * // when used inside a PreferenceKeyContext of value 'my-app'
 * const [theme, setTheme] = usePreference('theme', 'light');
 * // this is equivalent to
 * const [theme, setTheme] = useStore('my-app.theme', 'light');
 */
declare function usePreference<T>(key: string, defaultValue: T): [T, (value: T | ((value: T) => void), defaultValue?: T) => void];
declare function usePreference<T = undefined>(key: string, defaultValue?: T | undefined): [T | undefined, (value: T | ((value: T) => void), defaultValue?: T) => void];
declare function usePreference(): [
    unknown,
    (value: unknown | ((value: unknown) => void), defaultValue?: unknown) => void
];
export { usePreference };
//# sourceMappingURL=usePreference.d.ts.map