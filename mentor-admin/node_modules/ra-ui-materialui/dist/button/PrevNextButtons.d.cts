import * as React from 'react';
import { type RaRecord, UsePrevNextControllerProps } from 'ra-core';
import { type SxProps, type ComponentsOverrides, type Theme } from '@mui/material';
/**
 * A component used to render the previous and next buttons in a Show or Edit view.
 *
 * The `<PrevNextButtons>` component renders navigation buttons linking to
 * the next and previous records of the current resource, the current index
 * and the total number of records.
 *
 * It uses usePrevNextController to fetch the list of records.
 *
 * `<PrevNextButtons>` can be used anywhere a record context is provided
 * (often inside a `<Show>` or `<Edit>` component).
 *
 * @example <caption>navigate to edit view by default</caption>
 * <ShowButton />
 *
 * @example <caption>navigate to show view</caption>
 * <PrevNextButtons linkType="show" />
 *
 * @example <caption>custom storeKey</caption>
 * <PrevNextButtons storeKey="listStoreKey" />
 *
 * @example <caption>limit the number of records to fetch</caption>
 * <PrevNextButtons limit={500} />
 *
 * @example <caption>customize filters and sort order</caption>
 * <PrevNextButtons
 *     linkType="show"
 *     sort={{
 *         field: 'first_name',
 *         order: 'DESC',
 *     }}
 *     filter={{ q: 'East a' }}
 * />
 *
 * @example <caption>customize style</caption>
 * <PrevNextButtons
 *     sx={{
 *         color: 'blue',
 *         '& .RaPrevNextButton-list': {
 *             marginBottom: '20px',
 *             color: 'red',
 *         },
 *     }}
 * />
 *
 * @example <caption>in an edit view</caption>
 * import * as React from "react";
 * import { Edit, PrevNextButtons, ShowButton, SimpleForm, TopToolbar } from 'react-admin';
 *
 * const MyTopToolbar = ({ children }) => (
 *     <TopToolbar>
 *         {children}
 *     </TopToolbar>
 * );
 *
 * export const PostEdit = () => (
 *      <Edit
 *          actions={
 *              <MyTopToolbar>
 *                  <PrevNextButtons
 *                      sort={{
 *                          field: 'first_name',
 *                          order: 'DESC',
 *                      }}
 *                      filter={{ q: 'East a' }}
 *                  />
 *                  <ShowButton />
 *              </MyTopToolbar>
 *          }
 *      >
 *          <SimpleForm>...</SimpleForm>
 *      </Edit>
 * );
 */
export declare const PrevNextButtons: <RecordType extends RaRecord = any>(inProps: PrevNextButtonProps<RecordType>) => React.JSX.Element;
export interface PrevNextButtonProps<RecordType extends RaRecord = any> extends UsePrevNextControllerProps<RecordType> {
    sx?: SxProps<Theme>;
}
export declare const PrevNextButtonClasses: {
    root: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaPrevNextButton: 'root';
    }
    interface ComponentsPropsList {
        RaPrevNextButton: Partial<PrevNextButtonProps>;
    }
    interface Components {
        RaPrevNextButton?: {
            defaultProps?: ComponentsPropsList['RaPrevNextButton'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaPrevNextButton'];
        };
    }
}
//# sourceMappingURL=PrevNextButtons.d.ts.map