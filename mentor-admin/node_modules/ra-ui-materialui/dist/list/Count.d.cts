import * as React from 'react';
import { SortPayload } from 'ra-core';
import { TypographyProps } from '@mui/material';
/**
 * Fetch and render the number of records of a given resource
 *
 * Relies on dataProvider.getList() returning a total property
 *
 * @example // Display the number of records in the current resource (based on ResourceContext)
 * <Count />
 *
 * @example // Display the number of posts
 * <Count resource="posts" />
 *
 * @example // Display the number of published posts
 * <Count resource="posts" filter={{ is_published: true }}/>
 *
 * @example // Display the number of posts, with a custom Typography variant
 * <Count resource="posts" variant="h1" />
 *
 * @see ReferenceManyCount for a similar component which fetches the number of records related to the current one
 */
export declare const Count: (props: CountProps) => React.JSX.Element;
export interface CountProps extends TypographyProps {
    filter?: any;
    sort?: SortPayload;
    link?: Boolean;
    resource?: string;
    timeout?: number;
}
//# sourceMappingURL=Count.d.ts.map