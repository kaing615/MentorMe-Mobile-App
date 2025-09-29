import * as React from 'react';
/**
 * Test utility to simulate a device form factor for server-side mediaQueries
 *
 * Do not use inside a browser.
 *
 * @example
 *
 * <DeviceTestWrapper width="sm">
 *     <MyResponsiveComponent />
 * <DeviceTestWrapper>
 */
export declare const DeviceTestWrapper: ({ width, children, }: DeviceTestWrapperProps) => React.JSX.Element;
export interface DeviceTestWrapperProps {
    width: 'md' | 'xs' | 'sm' | 'lg' | 'xl';
    children: React.ReactNode;
}
//# sourceMappingURL=DeviceTestWrapper.d.ts.map