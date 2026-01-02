import * as React from "react";
import {
  List,
  Datagrid,
  TextField,
  DateField,
  NumberField,
  TextInput,
  SelectInput,
  Button,
  Confirm,
  Edit,
  EditButton,
  SimpleForm,
  DateTimeInput,
  NumberInput,
  useNotify,
  useRefresh,
  useRecordContext,
  required,
} from "react-admin";
import { Chip } from "@mui/material";

/* ===================== Types ===================== */

type Booking = {
  id: string | number;
  mentorId?: string | number;
  menteeId?: string | number;
  status?: "pending" | "confirmed" | "completed" | "cancelled";
  startTime?: string;
  endTime?: string;
  price?: number;
  createdAt?: string;
};

/* ===================== Constants ===================== */

const bookingStatusChoices = [
  { id: "pending", name: "Pending" },
  { id: "confirmed", name: "Confirmed" },
  { id: "completed", name: "Completed" },
  { id: "cancelled", name: "Cancelled" },
];

const apiUrl = import.meta.env.VITE_API_URL;
const token = () => localStorage.getItem("access_token") || "";

/* ===================== Filters ===================== */

const BookingFilters = [
  <TextInput key="mentorId" source="mentorId" label="Mentor ID" />,
  <TextInput key="menteeId" source="menteeId" label="Mentee ID" />,
  <SelectInput
    key="status"
    source="status"
    label="Status"
    choices={bookingStatusChoices}
    alwaysOn
  />,
];

/* ===================== UI Helpers ===================== */

function StatusChip() {
  const record = useRecordContext<Booking>();
  if (!record?.status) return null;

  const colorMap: any = {
    pending: "warning",
    confirmed: "info",
    completed: "success",
    cancelled: "error",
  };

  return (
    <Chip size="small" label={record.status} color={colorMap[record.status]} />
  );
}

/* ===================== Quick Action Button ===================== */

function ChangeStatusButton(props: {
  toStatus: Booking["status"];
  label: string;
}) {
  const { toStatus, label } = props;
  const record = useRecordContext<Booking>();
  const notify = useNotify();
  const refresh = useRefresh();
  const [open, setOpen] = React.useState(false);

  if (!record) return null;
  if (record.status === toStatus) return null;

  // business rule: completed/cancelled không đổi nữa
  if (["completed", "cancelled"].includes(record.status || "")) return null;

  const onConfirm = async () => {
    try {
      const res = await fetch(`${apiUrl}/bookings/${record.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token()}`,
        },
        body: JSON.stringify({ status: toStatus }),
      });

      if (!res.ok) throw new Error("Update failed");

      notify(`Booking ${toStatus}`, { type: "success" });
      refresh();
    } catch (e: any) {
      notify(e.message || "Update failed", { type: "error" });
    } finally {
      setOpen(false);
    }
  };

  return (
    <>
      <Button label={label} onClick={() => setOpen(true)} />
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

/* ===================== List ===================== */

export function BookingList() {
  return (
    <List
      filters={BookingFilters}
      sort={{ field: "startTime", order: "DESC" }}
      perPage={25}
      title="Bookings"
    >
      <Datagrid rowClick={false}>
        <TextField source="id" />
        <TextField source="mentorId" />
        <TextField source="menteeId" />
        <StatusChip />
        <DateField source="startTime" showTime />
        <DateField source="endTime" showTime />
        <NumberField source="price" />
        <EditButton />
        <ChangeStatusButton toStatus="confirmed" label="Confirm" />
        <ChangeStatusButton toStatus="completed" label="Complete" />
        <ChangeStatusButton toStatus="cancelled" label="Cancel" />
      </Datagrid>
    </List>
  );
}

/* ===================== Edit ===================== */

export function BookingEdit() {
  return (
    <Edit>
      <SimpleForm>
        <SelectInput
          source="status"
          choices={bookingStatusChoices}
          validate={[required()]}
        />
        <TextInput source="mentorId" validate={[required()]} />
        <TextInput source="menteeId" validate={[required()]} />
        <DateTimeInput source="startTime" validate={[required()]} />
        <DateTimeInput source="endTime" validate={[required()]} />
        <NumberInput source="price" />
      </SimpleForm>
    </Edit>
  );
}
