/**
 * Checks if the application code has changed and calls the provided onNewVersionAvailable function when needed.
 *
 * It checks for code update by downloading the provided URL (default to the HTML page) and
 * comparing the hash of the response with the hash of the current page.
 *
 * @param {UseCheckForApplicationUpdateOptions} options The options
 * @param {Function} options.onNewVersionAvailable The function to call when a new version of the application is available.
 * @param {string} options.url Optional. The URL to download to check for code update. Defaults to the current URL.
 * @param {RequestInit} options.fetchOptions Optional. The options passed to fetch function when checking for update.
 * @param {number} options.interval Optional. The interval in milliseconds between two checks. Defaults to 3600000 (1 hour).
 * @param {boolean} options.disabled Optional. Whether the check should be disabled. Defaults to false.
 */
export declare const useCheckForApplicationUpdate: (options: UseCheckForApplicationUpdateOptions) => void;
export interface UseCheckForApplicationUpdateOptions {
    onNewVersionAvailable: () => void;
    interval?: number;
    url?: string;
    fetchOptions?: RequestInit;
    disabled?: boolean;
}
//# sourceMappingURL=useCheckForApplicationUpdate.d.ts.map