import * as React from 'react';
import { ReactNode } from 'react';
import { BoxProps } from '@mui/material';
/**
 * This component offers a wrapper to render children inside a FilterList
 * section.
 *
 * It basically adds a header with an icon and a label, before rendering the
 * children.
 *
 * It is used by `<FilterList>`, but can also be used standalone to make your
 * own components look nicer alongside a filter list.
 *
 * @example
 * import MailIcon from '@mui/icons-material/MailOutline';
 * import TitleIcon from '@mui/icons-material/Title';
 * import { Card, CardContent } from '@mui/material';
 * import * as React from 'react';
 * import {
 *     FilterLiveForm,
 *     FilterList,
 *     FilterListItem,
 *     FilterListSection,
 *     TextInput,
 * } from 'react-admin';
 *
 * export const BookListAside = () => (
 *     <Card sx={{ order: -1, mr: 2, mt: 6, width: 250, height: 'fit-content' }}>
 *         <CardContent>
 *             <FilterList label="Subscribed to newsletter" icon={<MailIcon />}>
 *                 <FilterListItem label="Yes" value={{ has_newsletter: true }} />
 *                 <FilterListItem label="No" value={{ has_newsletter: false }} />
 *             </FilterList>
 *             <FilterListSection label="Title" icon={<TitleIcon />}>
 *                 <FilterLiveForm>
 *                     <TextInput source="title" resettable helperText={false} />
 *                 </FilterLiveForm>
 *             </FilterListSection>
 *         </CardContent>
 *     </Card>
 * );
 */
export declare const FilterListSection: (props: FilterListSectionProps) => React.JSX.Element;
export interface FilterListSectionProps extends BoxProps {
    label: string;
    icon: ReactNode;
}
//# sourceMappingURL=FilterListSection.d.ts.map