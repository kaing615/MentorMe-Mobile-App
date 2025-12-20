import * as React from "react";
import {
  Button,
  Confirm,
  Datagrid,
  DateField,
  DateTimeInput,
  Edit,
  EditButton,
  List,
  NumberField,
  NumberInput,
  SelectInput as RaSelectInput,
  TextInput as RaTextInput,
  required,
  SelectInput,
  SimpleForm,
  TextField,
  TextInput,
  useDataProvider,
  useNotify,
  useRecordContext,
  useRefresh,
} from "react-admin";

type Booking = {
  id: string | number;
  mentorId?: string | number;
  menteeId?: string | number;
  status?: "pending" | "confirmed" | "completed" | "cancelled";
  startTime?: string;
  endTime?: string;
  price?: number;
};

const bookingStatusChoices = [
  { id: "pending", name: "pending" },
  { id: "confirmed", name: "confirmed" },
  { id: "completed", name: "completed" },
  { id: "cancelled", name: "cancelled" },
];

const BookingFilters = [
  <TextInput key="mentorId" source="mentorId" label="Mentor ID" />,
  <TextInput key="menteeId" source="menteeId" label="Mentee ID" />,
  <SelectInput key="status" source="status" label="Status" choices={bookingStatusChoices} alwaysOn />,
];

function QuickStatusButton(props: { toStatus: Booking["status"]; label: string }) {
  const { toStatus, label } = props;
  const record = useRecordContext<Booking>();
  const dataProvider = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();
  const [open, setOpen] = React.useState(false);

  if (!record) return null;

  const disabled = record.status === toStatus;

  const onConfirm = async () => {
    try {
      await dataProvider.update("bookings", {
        id: record.id,
        data: { status: toStatus },
        previousData: record,
      });
      notify("Booking updated", { type: "success" });
      refresh();
    } catch (e: any) {
      notify(e?.message || "Update failed", { type: "error" });
    } finally {
      setOpen(false);
    }
  };

  return (
    <>
      <Button label={label} onClick={() => setOpen(true)} disabled={disabled} />
      <Confirm
        isOpen={open}
        title="Change booking status"
        content={`Change status to "${toStatus}"?`}
        onConfirm={onConfirm}
        onClose={() => setOpen(false)}
      />
    </>
  );
}

export function BookingList() {
  return (
    <List filters={BookingFilters} sort={{ field: "startTime", order: "DESC" }}>
      <Datagrid rowClick={false}>
        <TextField source="id" />
        <TextField source="mentorId" />
        <TextField source="menteeId" />
        <TextField source="status" />
        <DateField source="startTime" showTime />
        <DateField source="endTime" showTime />
        <NumberField source="price" />
        <EditButton />
        <QuickStatusButton toStatus="confirmed" label="Confirm" />
        <QuickStatusButton toStatus="completed" label="Complete" />
        <QuickStatusButton toStatus="cancelled" label="Cancel" />
      </Datagrid>
    </List>
  );
}

export function BookingEdit() {
  return (
    <Edit>
      <SimpleForm>
        <RaSelectInput source="status" choices={bookingStatusChoices} validate={[required()]} />
        <RaTextInput source="mentorId" validate={[required()]} />
        <RaTextInput source="menteeId" validate={[required()]} />
        <DateTimeInput source="startTime" validate={[required()]} />
        <DateTimeInput source="endTime" validate={[required()]} />
        <NumberInput source="price" />
      </SimpleForm>
    </Edit>
  );
}
