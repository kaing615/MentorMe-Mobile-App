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
  useNotify,
  useRefresh,
  useRecordContext,
  required,
} from "react-admin";
import { Chip, Stack } from "@mui/material";

/* ===================== Types ===================== */

type BookingStatus =
  | "PaymentPending"
  | "PendingMentor"
  | "Confirmed"
  | "Failed"
  | "Cancelled"
  | "Declined"
  | "Completed"
  | "NoShowMentor"
  | "NoShowMentee"
  | "NoShowBoth";

type Booking = {
  id: string | number;
  mentorId?: string;
  menteeId?: string;
  mentorFullName?: string;
  menteeFullName?: string;
  status?: BookingStatus;
  startTimeIso?: string;
  endTimeIso?: string;
  price?: number;
  topic?: string;
  notes?: string;
  meetingLink?: string;
  location?: string;
  cancelReason?: string;
  createdAt?: string;
};

/* ===================== Constants ===================== */

const bookingStatusChoices = [
  { id: "PaymentPending", name: "Payment Pending" },
  { id: "PendingMentor", name: "Pending Mentor" },
  { id: "Confirmed", name: "Confirmed" },
  { id: "Failed", name: "Failed" },
  { id: "Cancelled", name: "Cancelled" },
  { id: "Declined", name: "Declined" },
  { id: "Completed", name: "Completed" },
  { id: "NoShowMentor", name: "No Show (Mentor)" },
  { id: "NoShowMentee", name: "No Show (Mentee)" },
  { id: "NoShowBoth", name: "No Show (Both)" },
];

const terminalStatuses: BookingStatus[] = [
  "Failed",
  "Cancelled",
  "Declined",
  "Completed",
  "NoShowMentor",
  "NoShowMentee",
  "NoShowBoth",
];

const statusColors: Record<BookingStatus, "default" | "info" | "success" | "warning" | "error"> = {
  PaymentPending: "warning",
  PendingMentor: "warning",
  Confirmed: "info",
  Failed: "error",
  Cancelled: "error",
  Declined: "error",
  Completed: "success",
  NoShowMentor: "error",
  NoShowMentee: "error",
  NoShowBoth: "error",
};

const apiUrl = import.meta.env.VITE_API_URL;
const token = () => localStorage.getItem("access_token") || "";

const formatStatusLabel = (status: string) =>
  status.replace(/([a-z])([A-Z])/g, "$1 $2").replace(/_/g, " " );

/* ===================== Filters ===================== */

const BookingFilters = [
  <TextInput key="mentorId" source="mentorId" label="Mentor ID" />,
  <TextInput key="menteeId" source="menteeId" label="Mentee ID" />,
  <SelectInput
    key="status"
    source="status"
    label="Status"
    choices={bookingStatusChoices}
  />,
  <DateTimeInput key="from" source="from" label="From" />,
  <DateTimeInput key="to" source="to" label="To" />,
];

/* ===================== UI Helpers ===================== */

function StatusChip() {
  const record = useRecordContext<Booking>();
  if (!record?.status) return null;

  return (
    <Chip
      size="small"
      label={formatStatusLabel(record.status)}
      color={statusColors[record.status] || "default"}
    />
  );
}

/* ===================== Quick Action Button ===================== */

function ChangeStatusButton(props: {
  toStatus: BookingStatus;
  label: string;
  fromStatuses?: BookingStatus[];
}) {
  const { toStatus, label, fromStatuses } = props;
  const record = useRecordContext<Booking>();
  const notify = useNotify();
  const refresh = useRefresh();
  const [open, setOpen] = React.useState(false);

  if (!record?.status) return null;
  if (record.status === toStatus) return null;
  if (fromStatuses && !fromStatuses.includes(record.status)) return null;
  if (terminalStatuses.includes(record.status)) return null;

  const onConfirm = async () => {
    try {
      const res = await fetch(`${apiUrl}/admin/bookings/${record.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token()}`,
        },
        body: JSON.stringify({ status: toStatus }),
      });

      if (!res.ok) throw new Error("Update failed");

      notify(`Booking ${formatStatusLabel(toStatus)}`, { type: "success" });
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
        content={`Change status to "${formatStatusLabel(toStatus)}"?`}
        onConfirm={onConfirm}
        onClose={() => setOpen(false)}
      />
    </>
  );
}

function BookingActions() {
  const record = useRecordContext<Booking>();
  if (!record?.status) return null;

  return (
    <Stack direction="row" spacing={1}>
      <ChangeStatusButton
        toStatus="Confirmed"
        label="Confirm"
        fromStatuses={["PendingMentor"]}
      />
      <ChangeStatusButton
        toStatus="Declined"
        label="Decline"
        fromStatuses={["PendingMentor"]}
      />
      <ChangeStatusButton
        toStatus="Completed"
        label="Complete"
        fromStatuses={["Confirmed"]}
      />
      <ChangeStatusButton
        toStatus="Cancelled"
        label="Cancel"
        fromStatuses={["PaymentPending", "PendingMentor", "Confirmed"]}
      />
      <ChangeStatusButton
        toStatus="Failed"
        label="Fail"
        fromStatuses={["PaymentPending"]}
      />
    </Stack>
  );
}

/* ===================== List ===================== */

export function BookingList() {
  return (
    <List
      filters={BookingFilters}
      sort={{ field: "startTimeIso", order: "DESC" }}
      perPage={25}
      title="Bookings"
    >
      <Datagrid rowClick={false}>
        <TextField source="id" />
        <TextField source="mentorFullName" label="Mentor" />
        <TextField source="menteeFullName" label="Mentee" />
        <TextField source="mentorId" />
        <TextField source="menteeId" />
        <StatusChip />
        <DateField source="startTimeIso" showTime />
        <DateField source="endTimeIso" showTime />
        <NumberField source="price" />
        <TextField source="topic" />
        <DateField source="createdAt" showTime />
        <EditButton />
        <BookingActions />
      </Datagrid>
    </List>
  );
}

/* ===================== Edit ===================== */

export function BookingEdit() {
  return (
    <Edit>
      <SimpleForm>
        <TextField source="id" />
        <TextField source="mentorFullName" label="Mentor" />
        <TextField source="menteeFullName" label="Mentee" />
        <TextField source="mentorId" />
        <TextField source="menteeId" />
        <DateField source="startTimeIso" showTime />
        <DateField source="endTimeIso" showTime />
        <NumberField source="price" />
        <SelectInput
          source="status"
          choices={bookingStatusChoices}
          validate={[required()]}
        />
        <TextInput source="topic" fullWidth />
        <TextInput source="meetingLink" fullWidth />
        <TextInput source="location" fullWidth />
        <TextInput source="notes" fullWidth multiline minRows={3} />
        <TextInput
          source="cancelReason"
          fullWidth
          multiline
          minRows={2}
        />
      </SimpleForm>
    </Edit>
  );
}
