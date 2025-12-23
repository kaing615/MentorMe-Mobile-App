/**
 * A version of lodash/debounce that always returns a promise but wait for the debounced function to return to resolve it.
 * @param func The function to debounce
 * @param wait The debounce delay
 * @returns A debounced function that returns a promise
 */
export declare function asyncDebounce<FunctionType extends (...args: any[]) => Promise<any>>(func: FunctionType, wait?: number): (...args: Parameters<FunctionType>) => ReturnType<FunctionType>;
//# sourceMappingURL=asyncDebounce.d.ts.map