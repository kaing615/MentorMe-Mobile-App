import * as React from 'react';
import { Location, NavigateFunction } from 'react-router-dom';
import type { InitialEntry } from '@remix-run/router';
/**
 * Wrapper around react-router's `createMemoryRouter` to be used in test components.
 *
 * It is similar to `MemoryRouter` but it supports
 * [data APIs](https://reactrouter.com/en/main/routers/picking-a-router#data-apis).
 *
 * Additionally, it provides
 * - a `locationCallback` prop to get the location in the test
 * - a `navigateCallback` prop to be able to navigate in the test
 */
export declare const TestMemoryRouter: ({ children, locationCallback, navigateCallback, ...rest }: {
    children: React.ReactNode;
    locationCallback?: (l: Location) => void;
    navigateCallback?: (n: NavigateFunction) => void;
    basename?: string;
    initialEntries?: InitialEntry[];
    initialIndex?: number;
}) => React.JSX.Element;
//# sourceMappingURL=TestMemoryRouter.d.ts.map