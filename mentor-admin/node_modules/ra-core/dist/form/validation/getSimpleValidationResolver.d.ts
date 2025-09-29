import { FieldValues } from 'react-hook-form';
/**
 * Convert a simple validation function that returns an object matching the form shape with errors
 * to a validation resolver compatible with react-hook-form.
 *
 * @example
 * const validate = (values: any) => {
 *     if (values.username == null || values.username.trim() === '') {
 *         return { username: 'Required' };
 *     }
 * }
 *
 * const validationResolver = getSimpleValidationResolver(validate);
 *
 * const UserForm = () => (
 *     <Form
 *         defaultValues={{ username: 'John' }}
 *         validationResolver={validationResolver}
 *     >
 *         <TextField source="username" />
 *     </Form>
 * );
 */
export declare const getSimpleValidationResolver: (validate: ValidateForm) => (data: FieldValues) => Promise<{
    values: FieldValues;
    errors: {};
} | {
    values: {};
    errors: any;
}>;
export type ValidateForm = (data: FieldValues) => FieldValues | Promise<FieldValues>;
//# sourceMappingURL=getSimpleValidationResolver.d.ts.map