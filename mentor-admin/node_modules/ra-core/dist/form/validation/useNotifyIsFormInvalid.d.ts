import { Control } from 'react-hook-form';
/**
 * This hook display an error message on submit in Form and SaveButton.
 *
 * We can't do the form validity check in the form submit handler
 * as the form state may not have been updated yet when onSubmit validation mode is enabled
 * or when the form hasn't been touched at all.
 */
export declare const useNotifyIsFormInvalid: (control?: Control, enabled?: boolean) => void;
//# sourceMappingURL=useNotifyIsFormInvalid.d.ts.map