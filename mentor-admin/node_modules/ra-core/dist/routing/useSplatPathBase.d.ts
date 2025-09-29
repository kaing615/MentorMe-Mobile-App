/**
 * Utility hook to get the base path of a splat path.
 * Compatible both with react-router v6 and v7.
 *
 * Example:
 * If a splat path is defined as `/posts/:id/show/*`,
 * and the current location is `/posts/12/show/3`,
 * this hook will return `/posts/12/show`.
 *
 * Solution inspired by
 * https://github.com/remix-run/react-router/issues/11052#issuecomment-1828470203
 */
export declare const useSplatPathBase: () => string;
//# sourceMappingURL=useSplatPathBase.d.ts.map