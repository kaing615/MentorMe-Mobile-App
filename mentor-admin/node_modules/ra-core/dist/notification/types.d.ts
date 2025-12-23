import { ReactNode } from 'react';
export type NotificationType = 'success' | 'info' | 'warning' | 'error';
export interface NotificationOptions {
    autoHideDuration?: number | null;
    messageArgs?: any;
    multiLine?: boolean;
    undoable?: boolean;
    [key: string]: any;
}
export interface NotificationPayload {
    readonly message: string | ReactNode;
    readonly type: NotificationType;
    readonly notificationOptions?: NotificationOptions;
}
//# sourceMappingURL=types.d.ts.map