/**
 * Hook somewhat equivalent to useEvent, but with a debounce
 * Returns a debounced callback which will not change across re-renders unless the
 * callback or delay changes
 * @see https://reactjs.org/docs/hooks-faq.html#how-to-read-an-often-changing-value-from-usecallback
 * @see https://github.com/facebook/react/issues/14099#issuecomment-440013892
 */
export declare const useDebouncedEvent: <Args extends unknown[], Return>(callback: (...args: Args) => Return, delay: number) => (...args: Args) => Return | undefined;
//# sourceMappingURL=useDebouncedEvent.d.ts.map