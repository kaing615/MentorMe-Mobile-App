import * as React from 'react';
/**
 * Exposes and manages a queue of undoable mutations
 *
 * This context is used in CoreAdminContext so that every react-admin app
 * can use the useAddUndoableMutation and useTakeUndoableMutation hooks.
 *
 * Note: We need a separate queue for mutations (instead of using the
 * notifications queue) because the mutations are not dequeued when the
 * notification is displayed, but when it is dismissed.
 */
export declare const UndoableMutationsContextProvider: ({ children }: {
    children: any;
}) => React.JSX.Element;
//# sourceMappingURL=UndoableMutationsContextProvider.d.ts.map