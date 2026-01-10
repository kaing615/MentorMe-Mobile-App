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
  { id: "PaymentPending", name: "Chờ thanh toán" },
  { id: "PendingMentor", name: "Chờ mentor xác nhận" },
  { id: "Confirmed", name: "Đã xác nhận" },
  { id: "Failed", name: "Thất bại" },
  { id: "Cancelled", name: "Đã hủy" },
  { id: "Declined", name: "Từ chối" },
  { id: "Completed", name: "Hoàn thành" },
  { id: "NoShowMentor", name: "Mentor vắng mặt" },
  { id: "NoShowMentee", name: "Mentee vắng mặt" },
  { id: "NoShowBoth", name: "Cả hai vắng mặt" },
];

const bookingStatusLabelMap = bookingStatusChoices.reduce<Record<string, string>>(
  (acc, choice) => {
    acc[choice.id] = choice.name;
    return acc;
  },
  {}
);

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

const getStatusLabel = (status?: BookingStatus) =>
  status ? bookingStatusLabelMap[status] || status : "";

/* ===================== Filters ===================== */

const BookingFilters = [
  <TextInput key="mentorId" source="mentorId" label="ID mentor" />,
  <TextInput key="menteeId" source="menteeId" label="ID mentee" />,
  <SelectInput
    key="status"
    source="status"
    label="Trạng thái"
    choices={bookingStatusChoices}
  />,
  <DateTimeInput key="from" source="from" label="Từ" />,
  <DateTimeInput key="to" source="to" label="Đến" />,
];

/* ===================== UI Helpers ===================== */

function StatusChip({ label: _label }: { label?: string }) {
  const record = useRecordContext<Booking>();
  if (!record?.status) return null;

  return (
    <Chip
      size="small"
      label={getStatusLabel(record.status)}
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

      if (!res.ok) throw new Error("Cập nhật thất bại");

      notify(`Đã cập nhật lịch hẹn: ${getStatusLabel(toStatus)}`, {
        type: "success",
      });
      refresh();
    } catch (e: any) {
      notify(e.message || "Cập nhật thất bại", { type: "error" });
    } finally {
      setOpen(false);
    }
  };

  return (
    <>
      <Button label={label} onClick={() => setOpen(true)} />
      <Confirm
        isOpen={open}
        title="Cập nhật trạng thái lịch hẹn"
        content={`Chuyển trạng thái thành "${getStatusLabel(toStatus)}"?`}
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
        label="Xác nhận"
        fromStatuses={["PendingMentor"]}
      />
      <ChangeStatusButton
        toStatus="Declined"
        label="Từ chối"
        fromStatuses={["PendingMentor"]}
      />
      <ChangeStatusButton
        toStatus="Completed"
        label="Hoàn thành"
        fromStatuses={["Confirmed"]}
      />
      <ChangeStatusButton
        toStatus="Cancelled"
        label="Hủy"
        fromStatuses={["PaymentPending", "PendingMentor", "Confirmed"]}
      />
      <ChangeStatusButton
        toStatus="Failed"
        label="Thất bại"
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
      title="Lịch hẹn"
    >
      <Datagrid rowClick={false}>
        <TextField source="id" label="Mã lịch hẹn" />
        <TextField source="mentorFullName" label="Mentor" />
        <TextField source="menteeFullName" label="Mentee" />
        <TextField source="mentorId" label="ID mentor" />
        <TextField source="menteeId" label="ID mentee" />
        <StatusChip label="Trạng thái" />
        <DateField source="startTimeIso" showTime label="Bắt đầu" />
        <DateField source="endTimeIso" showTime label="Kết thúc" />
        <NumberField source="price" label="Giá" />
        <TextField source="topic" label="Chủ đề" />
        <DateField source="createdAt" showTime label="Tạo lúc" />
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
        <TextField source="id" label="Mã lịch hẹn" />
        <TextField source="mentorFullName" label="Mentor" />
        <TextField source="menteeFullName" label="Mentee" />
        <TextField source="mentorId" label="ID mentor" />
        <TextField source="menteeId" label="ID mentee" />
        <DateField source="startTimeIso" showTime label="Bắt đầu" />
        <DateField source="endTimeIso" showTime label="Kết thúc" />
        <NumberField source="price" label="Giá" />
        <SelectInput
          source="status"
          label="Trạng thái"
          choices={bookingStatusChoices}
          validate={[required()]}
        />
        <TextInput source="topic" label="Chủ đề" fullWidth />
        <TextInput source="meetingLink" label="Link phòng họp" fullWidth />
        <TextInput source="location" label="Địa điểm" fullWidth />
        <TextInput source="notes" label="Ghi chú" fullWidth multiline minRows={3} />
        <TextInput
          source="cancelReason"
          label="Lý do hủy"
          fullWidth
          multiline
          minRows={2}
        />
      </SimpleForm>
    </Edit>
  );
}
