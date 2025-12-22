/**
 * Get the current locale and the ability to change it
 *
 * @example
 *
 * import { useLocaleState } from 'react-admin';
 *
 * const availableLanguages = {
 *     en: 'English',
 *     fr: 'Français',
 * }
 * const LocaleSwitcher = () => {
 *     const [locale, setLocale] = useLocaleState();
 *     return (
 *         <div>
 *             <div>Language</div>
 *             <Button disabled={locale === 'fr'} onClick={() => setLocale('fr')}>
 *                 English
 *             </Button>
 *             <Button disabled={locale === 'en'} onClick={() => setLocale('en')}>
 *                 French
 *             </Button>
 *         </div>
 *     );
 * };
 */
export declare const useLocaleState: () => [string, (value: string | ((value: string) => void), defaultValue?: string | undefined) => void];
//# sourceMappingURL=useLocaleState.d.ts.map