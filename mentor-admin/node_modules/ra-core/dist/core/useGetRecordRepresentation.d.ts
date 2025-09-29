import { ReactNode } from 'react';
/**
 * Get the default representation of a record (either a string or a React node)
 *
 * @example // No customization
 * const getRecordRepresentation = useGetRecordRepresentation('posts');
 * getRecordRepresentation({ id: 1, title: 'Hello' }); // => "#1"
 *
 * @example // With <Resource name="posts" recordRepresentation="title" />
 * const getRecordRepresentation = useGetRecordRepresentation('posts');
 * getRecordRepresentation({ id: 1, title: 'Hello' }); // => "Hello"
 */
export declare const useGetRecordRepresentation: (resource?: string) => ((record: any) => ReactNode);
//# sourceMappingURL=useGetRecordRepresentation.d.ts.map