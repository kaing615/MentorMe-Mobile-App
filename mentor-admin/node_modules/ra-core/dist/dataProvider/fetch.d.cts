import { stringify } from 'query-string';
export interface Options extends RequestInit {
    user?: {
        authenticated?: boolean;
        token?: string;
    };
}
export declare const createHeadersFromOptions: (options: Options) => Headers;
/**
 * Utility function to make HTTP calls. It's similar to the HTML5 `fetch()`, except it handles JSON decoding and HTTP error codes automatically.
 *
 * @param url the URL to call
 * @param options the options to pass to the HTTP call
 * @param options.user the user object, used for the Authorization header
 * @param options.user.token the token to pass as the Authorization header
 * @param options.user.authenticated whether the user is authenticated or not (the Authorization header will be set only if this is true)
 * @param options.headers the headers to pass to the HTTP call
 *
 * @returns {Promise} the Promise for a response object containing the following properties:
 * - status: the HTTP status code
 * - headers: the HTTP headers
 * - body: the response body
 * - json: the response body parsed as JSON
 */
export declare const fetchJson: (url: any, options?: Options) => Promise<{
    status: number;
    headers: Headers;
    body: string;
    json: any;
}>;
export declare const queryParameters: typeof stringify;
export declare const flattenObject: (value: any, path?: string[]) => any;
//# sourceMappingURL=fetch.d.ts.map