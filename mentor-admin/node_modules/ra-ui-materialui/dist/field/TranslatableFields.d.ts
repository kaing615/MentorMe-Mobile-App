import * as React from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import type { ReactElement, ReactNode } from 'react';
import { type UseTranslatableOptions, type RaRecord } from 'ra-core';
/**
 * Provides a way to show multiple languages for any field passed as children.
 * It expects the translatable values to have the following structure:
 * {
 *     name: {
 *         en: 'The english value',
 *         fr: 'The french value',
 *         tlh: 'The klingon value',
 *     },
 *     description: {
 *         en: 'The english value',
 *         fr: 'The french value',
 *         tlh: 'The klingon value',
 *     }
 * }
 *
 * @example <caption>Basic usage</caption>
 * <TranslatableFields locales={['en', 'fr']}>
 *     <TextField source={getSource('title')} />
 *     <TextField source={getSource('description')} />
 * </TranslatableFields>
 *
 * @example <caption>With a custom language selector</caption>
 * <TranslatableFields
 *     selector={<MyLanguageSelector />}
 *     locales={['en', 'fr']}
 * >
 *     <TextField source={getSource('title')} />
 * <TranslatableFields>
>
 *
 * const MyLanguageSelector = () => {
 *     const {
 *         locales,
 *         selectedLocale,
 *         selectLocale,
 *     } = useTranslatableContext();
 *
 *     return (
 *         <select onChange={selectLocale}>
 *             {locales.map((locale) => (
 *                 <option selected={locale.locale === selectedLocale}>
 *                     {locale.name}
 *                 </option>
 *             ))}
 *        </select>
 *     );
 * }
 *
 * @param props The component props
 * @param {string} props.defaultLocale The locale selected by default. Default to 'en'.
 * @param {string[]} props.locales An array of the possible locales in the form. For example [{ 'en', 'fr' }].
 * @param {ReactElement} props.selector The element responsible for selecting a locale. Defaults to Material UI tabs.
 */
export declare const TranslatableFields: (inProps: TranslatableFieldsProps) => React.JSX.Element;
export interface TranslatableFieldsProps extends UseTranslatableOptions {
    children: ReactNode;
    className?: string;
    record?: RaRecord;
    resource?: string;
    selector?: ReactElement;
    groupKey?: string;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaTranslatableFields: 'root';
    }
    interface ComponentsPropsList {
        RaTranslatableFields: Partial<TranslatableFieldsProps>;
    }
    interface Components {
        RaTranslatableFields?: {
            defaultProps?: ComponentsPropsList['RaTranslatableFields'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaTranslatableFields'];
        };
    }
}
//# sourceMappingURL=TranslatableFields.d.ts.map