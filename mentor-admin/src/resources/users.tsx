import * as React from "react";
import {
  List,
  Datagrid,
  TextField,
  EmailField,
  DateField,
  TextInput,
  SelectInput,
  BooleanField,
  Edit,
  SimpleForm,
  required,
  useNotify,
  useRefresh,
  useRecordContext,
  Confirm,
} from "react-admin";
import {
  Box,
  Chip,
  IconButton,
  Tooltip,
  Stack,
  Typography,
  Avatar,
} from "@mui/material";

import EditIcon from "@mui/icons-material/EditOutlined";
import LockIcon from "@mui/icons-material/LockOutlined";
import LockOpenIcon from "@mui/icons-material/LockOpenOutlined";
import PersonIcon from "@mui/icons-material/PersonOutline";

/* ===================== TYPES ===================== */

type User = {
  id: string;
  userName?: string;
  email?: string;
  role?: "root" | "admin" | "mentor" | "mentee";
  status?: string;
  isBlocked?: boolean;
  createdAt?: string;
};

/* ===================== FILTERS ===================== */

const roleChoices = [
  { id: "root", name: "Root" },
  { id: "admin", name: "Admin" },
  { id: "mentor", name: "Mentor" },
  { id: "mentee", name: "Mentee" },
];

const statusChoices = [
  { id: "active", name: "Active" },
  { id: "pending-mentor", name: "Pending Mentor" },
  { id: "verifying", name: "Verifying" },
  { id: "onboarding", name: "Onboarding" },
];

const UserFilters = [
  <TextInput key="q" source="q" label="Search" alwaysOn />,
  <SelectInput key="role" source="role" label="Role" choices={roleChoices} />,
  <SelectInput
    key="status"
    source="status"
    label="Status"
    choices={statusChoices}
  />,
];

/* ===================== FIELDS ===================== */

function UserIdentity() {
  const record = useRecordContext<User>();
  if (!record) return null;

  return (
    <Stack direction="row" spacing={2} alignItems="center">
      <Avatar sx={{ width: 36, height: 36 }}>
        <PersonIcon fontSize="small" />
      </Avatar>

      <Box>
        <Typography fontWeight={600}>{record.userName}</Typography>
        <Typography variant="body2" color="text.secondary">
          {record.email}
        </Typography>
      </Box>
    </Stack>
  );
}

function ShortId() {
  const record = useRecordContext<User>();
  if (!record?.id) return null;

  return (
    <Typography variant="caption" color="text.secondary">
      #{record.id.slice(-6)}
    </Typography>
  );
}

function RoleChip() {
  const record = useRecordContext<User>();
  if (!record?.role) return null;

  const map: any = {
    root: "error",
    admin: "warning",
    mentor: "info",
    mentee: "default",
  };

  return (
    <Chip
      size="small"
      label={record.role.toUpperCase()}
      color={map[record.role]}
      sx={{ fontWeight: 600 }}
    />
  );
}

function StatusChip() {
  const record = useRecordContext<User>();
  if (!record?.status) return null;

  const map: any = {
    active: "success",
    "pending-mentor": "warning",
    verifying: "info",
    onboarding: "default",
  };

  return (
    <Chip
      size="small"
      label={record.status}
      color={map[record.status]}
      variant="outlined"
    />
  );
}

/* ===================== ACTIONS ===================== */

function UserActions() {
  const record = useRecordContext<User>();
  const notify = useNotify();
  const refresh = useRefresh();
  const [open, setOpen] = React.useState(false);

  if (!record) return null;

  const toggleBlock = async () => {
    try {
      await fetch(`${import.meta.env.VITE_API_URL}/users/${record.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("access_token")}`,
        },
        body: JSON.stringify({ isBlocked: !record.isBlocked }),
      });

      notify("User updated", { type: "success" });
      refresh();
    } catch {
      notify("Update failed", { type: "error" });
    } finally {
      setOpen(false);
    }
  };

  return (
    <Stack direction="row" spacing={1}>
      <Tooltip title="Edit">
        <IconButton size="small" href={`#/users/${record.id}`}>
          <EditIcon fontSize="small" />
        </IconButton>
      </Tooltip>

      <Tooltip title={record.isBlocked ? "Unlock" : "Lock"}>
        <IconButton
          size="small"
          color={record.isBlocked ? "success" : "error"}
          onClick={() => setOpen(true)}
        >
          {record.isBlocked ? <LockOpenIcon /> : <LockIcon />}
        </IconButton>
      </Tooltip>

      <Confirm
        isOpen={open}
        title={record.isBlocked ? "Unlock user" : "Lock user"}
        content={`Are you sure you want to ${
          record.isBlocked ? "unlock" : "lock"
        } this user?`}
        confirmColor={record.isBlocked ? "primary" : "warning"}
        onConfirm={toggleBlock}
        onClose={() => setOpen(false)}
      />
    </Stack>
  );
}

/* ===================== LIST ===================== */

export function UserList() {
  return (
    <List
      filters={UserFilters}
      sort={{ field: "createdAt", order: "DESC" }}
      perPage={25}
      title="Users"
    >
      <Datagrid
        rowClick={false}
        sx={{
          "& .RaDatagrid-row": {
            height: 72,
          },
          "& .MuiTableCell-root": {
            verticalAlign: "middle",
          },
        }}
      >
        <ShortId />
        <UserIdentity />
        <RoleChip />
        <StatusChip />
        <BooleanField source="isBlocked" label="Blocked" />
        <DateField source="createdAt" showTime />
        <UserActions />
      </Datagrid>
    </List>
  );
}

/* ===================== EDIT ===================== */

export function UserEdit() {
  return (
    <Edit>
      <SimpleForm>
        <TextInput source="userName" validate={[required()]} fullWidth />
        <TextInput source="email" validate={[required()]} fullWidth />
        <SelectInput
          source="role"
          choices={roleChoices}
          validate={[required()]}
        />
        <SelectInput source="status" choices={statusChoices} />
        <BooleanField source="isBlocked" />
      </SimpleForm>
    </Edit>
  );
}
