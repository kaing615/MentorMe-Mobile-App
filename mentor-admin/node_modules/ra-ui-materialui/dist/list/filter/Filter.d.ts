import * as React from 'react';
import { ReactNode } from 'react';
/**
 * Filter button/form combo
 *
 * @example
 *
 * const PostFilter = (props) => (
 *     <Filter {...props}>
 *         <TextInput label="Search" source="q" alwaysOn />
 *         <TextInput label="Title" source="title" defaultValue="Hello, World!" />
 *     </Filter>
 * );
 *
 * export const PostList = () => (
 *     <List filters={<PostFilter />}>
 *         ...
 *     </List>
 * );
 *
 */
export declare const Filter: (props: FilterProps) => React.JSX.Element;
export declare const FilterClasses: {
    button: string;
    form: string;
};
export interface FilterProps {
    children: ReactNode;
    context?: 'form' | 'button';
    variant?: string;
}
//# sourceMappingURL=Filter.d.ts.map