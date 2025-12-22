/**
 * A hook that gets the available locales from the i18nProvider.
 * @example
 *
 * import { useLocales } from 'react-admin';
 *
 * const LocaleSelector = () => {
 *     const locales = useLocales();
 *     const [currentLocale, setCurrentLocale] = useLocaleState();
 *
 *     return (
 *         <select onChange={event => setCurrentLocale(event.target.value)}>
 *             {locales.map(locale => (
 *                 <option key={locale.locale} value={locale.locale}>
 *                     {locale.name}
 *                 </option>
 *             )}
 *         </select>
 *     );
 * }
 */
export declare const useLocales: (options?: UseLocalesOptions) => {
    locale: string;
    name: string;
}[];
export interface UseLocalesOptions {
    locales?: {
        locale: string;
        name: string;
    }[];
}
//# sourceMappingURL=useLocales.d.ts.map