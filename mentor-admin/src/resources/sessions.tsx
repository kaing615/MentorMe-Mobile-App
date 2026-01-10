import * as React from "react";
import {
  List,
  Datagrid,
  TextField,
  DateField,
  NumberField,
  TextInput,
  SelectInput,
  DateTimeInput,
  useRecordContext,
} from "react-admin";
import { Chip } from "@mui/material";

/* ===================== Types ===================== */

type SessionLog = {
  id: string | number;
  bookingId?: string;
  mentorId?: string;
  menteeId?: string;
  status?: "waiting" | "active" | "ended" | "no_show";
  scheduledStart?: string;
  scheduledEnd?: string;
  actualStart?: string;
  actualEnd?: string;
  durationSec?: number;
  endReason?: string;
  waitingRoomMs?: number;
  mentorDisconnects?: number;
  menteeDisconnects?: number;
  createdAt?: string;
};

/* ===================== Constants ===================== */

const statusChoices = [
  { id: "waiting", name: "Chờ bắt đầu" },
  { id: "active", name: "Đang diễn ra" },
  { id: "ended", name: "Đã kết thúc" },
  { id: "no_show", name: "Vắng mặt" },
];

const statusLabelMap = statusChoices.reduce<Record<string, string>>((acc, choice) => {
  acc[choice.id] = choice.name;
  return acc;
}, {});

/* ===================== Filters ===================== */

const SessionFilters = [
  <TextInput key="bookingId" source="bookingId" label="ID lịch hẹn" />,
  <TextInput key="mentorId" source="mentorId" label="ID mentor" />,
  <TextInput key="menteeId" source="menteeId" label="ID mentee" />,
  <SelectInput
    key="status"
    source="status"
    label="Trạng thái"
    choices={statusChoices}
  />,
  <DateTimeInput key="from" source="from" label="Từ" />,
  <DateTimeInput key="to" source="to" label="Đến" />,
];

/* ===================== UI Helpers ===================== */

function StatusChip({ label: _label }: { label?: string }) {
  const record = useRecordContext<SessionLog>();
  if (!record?.status) return null;

  const colorMap: Record<string, "default" | "info" | "success" | "warning" | "error"> = {
    waiting: "warning",
    active: "info",
    ended: "success",
    no_show: "error",
  };

  return (
    <Chip
      size="small"
      label={statusLabelMap[record.status] || record.status}
      color={colorMap[record.status] || "default"}
    />
  );
}

/* ===================== List ===================== */

export function SessionList() {
  return (
    <List
      filters={SessionFilters}
      sort={{ field: "scheduledStart", order: "DESC" }}
      perPage={25}
      title="Phiên học"
    >
      <Datagrid rowClick={false}>
        <TextField source="id" label="Mã phiên" />
        <TextField source="bookingId" label="ID lịch hẹn" />
        <TextField source="mentorId" label="ID mentor" />
        <TextField source="menteeId" label="ID mentee" />
        <StatusChip label="Trạng thái" />
        <DateField source="scheduledStart" showTime label="Bắt đầu dự kiến" />
        <DateField source="scheduledEnd" showTime label="Kết thúc dự kiến" />
        <DateField source="actualStart" showTime label="Bắt đầu thực tế" />
        <DateField source="actualEnd" showTime label="Kết thúc thực tế" />
        <NumberField source="durationSec" label="Thời lượng (giây)" />
        <TextField source="endReason" label="Lý do kết thúc" />
        <NumberField source="waitingRoomMs" label="Thời gian chờ (ms)" />
        <NumberField source="mentorDisconnects" label="Mentor ngắt kết nối" />
        <NumberField source="menteeDisconnects" label="Mentee ngắt kết nối" />
        <DateField source="createdAt" showTime label="Tạo lúc" />
      </Datagrid>
    </List>
  );
}
