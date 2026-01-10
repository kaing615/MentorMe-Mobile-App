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
  { id: "waiting", name: "Waiting" },
  { id: "active", name: "Active" },
  { id: "ended", name: "Ended" },
  { id: "no_show", name: "No Show" },
];

/* ===================== Filters ===================== */

const SessionFilters = [
  <TextInput key="bookingId" source="bookingId" label="Booking ID" />,
  <TextInput key="mentorId" source="mentorId" label="Mentor ID" />,
  <TextInput key="menteeId" source="menteeId" label="Mentee ID" />,
  <SelectInput
    key="status"
    source="status"
    label="Status"
    choices={statusChoices}
  />,
  <DateTimeInput key="from" source="from" label="From" />,
  <DateTimeInput key="to" source="to" label="To" />,
];

/* ===================== UI Helpers ===================== */

function StatusChip() {
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
      label={record.status.replace("_", " ")}
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
      title="Sessions"
    >
      <Datagrid rowClick={false}>
        <TextField source="id" />
        <TextField source="bookingId" />
        <TextField source="mentorId" />
        <TextField source="menteeId" />
        <StatusChip />
        <DateField source="scheduledStart" showTime />
        <DateField source="scheduledEnd" showTime />
        <DateField source="actualStart" showTime />
        <DateField source="actualEnd" showTime />
        <NumberField source="durationSec" />
        <TextField source="endReason" />
        <NumberField source="waitingRoomMs" />
        <NumberField source="mentorDisconnects" />
        <NumberField source="menteeDisconnects" />
        <DateField source="createdAt" showTime />
      </Datagrid>
    </List>
  );
}
