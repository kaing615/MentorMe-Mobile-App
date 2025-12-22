import { Control } from 'react-hook-form';
/**
 * Display a confirmation dialog if the form has unsaved changes.
 * - If the user confirms, the navigation continues and the changes are lost.
 * - If the user cancels, the navigation is cancelled and the changes are kept.
 */
export declare const useWarnWhenUnsavedChanges: (enable: boolean, formRootPathname?: string, control?: Control) => void;
//# sourceMappingURL=useWarnWhenUnsavedChanges.d.ts.map