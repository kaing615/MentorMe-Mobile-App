import * as React from "react";
import {
  List,
  Datagrid,
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
  { id: "root", name: "Quản trị hệ thống" },
  { id: "admin", name: "Quản trị viên" },
  { id: "mentor", name: "Mentor" },
  { id: "mentee", name: "Mentee" },
];

const roleLabelMap = roleChoices.reduce<Record<string, string>>((acc, choice) => {
  acc[choice.id] = choice.name;
  return acc;
}, {});

const statusChoices = [
  { id: "active", name: "Hoạt động" },
  { id: "pending-mentor", name: "Chờ duyệt mentor" },
  { id: "verifying", name: "Đang xác minh" },
  { id: "onboarding", name: "Hướng dẫn ban đầu" },
];

const statusLabelMap = statusChoices.reduce<Record<string, string>>((acc, choice) => {
  acc[choice.id] = choice.name;
  return acc;
}, {});

const UserFilters = [
  <TextInput key="q" source="q" label="Tìm kiếm" alwaysOn />,
  <SelectInput key="role" source="role" label="Vai trò" choices={roleChoices} />,
  <SelectInput
    key="status"
    source="status"
    label="Trạng thái"
    choices={statusChoices}
  />,
];

/* ===================== FIELDS ===================== */

function UserIdentity({ label: _label }: { label?: string }) {
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

function ShortId({ label: _label }: { label?: string }) {
  const record = useRecordContext<User>();
  if (!record?.id) return null;

  return (
    <Typography variant="caption" color="text.secondary">
      #{record.id.slice(-6)}
    </Typography>
  );
}

function RoleChip({ label: _label }: { label?: string }) {
  const record = useRecordContext<User>();
  if (!record?.role) return null;

  const map: Record<string, "default" | "info" | "success" | "warning" | "error"> = {
    root: "error",
    admin: "warning",
    mentor: "info",
    mentee: "default",
  };

  return (
    <Chip
      size="small"
      label={roleLabelMap[record.role] || record.role.toUpperCase()}
      color={map[record.role]}
      sx={{ fontWeight: 600 }}
    />
  );
}

function StatusChip({ label: _label }: { label?: string }) {
  const record = useRecordContext<User>();
  if (!record?.status) return null;

  const map: Record<string, "default" | "info" | "success" | "warning" | "error"> = {
    active: "success",
    "pending-mentor": "warning",
    verifying: "info",
    onboarding: "default",
  };

  return (
    <Chip
      size="small"
      label={statusLabelMap[record.status] || record.status}
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

      notify("Đã cập nhật người dùng", { type: "success" });
      refresh();
    } catch {
      notify("Cập nhật thất bại", { type: "error" });
    } finally {
      setOpen(false);
    }
  };

  return (
    <Stack direction="row" spacing={1}>
      <Tooltip title="Chỉnh sửa">
        <IconButton size="small" href={`#/users/${record.id}`}>
          <EditIcon fontSize="small" />
        </IconButton>
      </Tooltip>

      <Tooltip title={record.isBlocked ? "Mở khóa" : "Khóa"}>
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
        title={record.isBlocked ? "Mở khóa người dùng" : "Khóa người dùng"}
        content={`Bạn có chắc muốn ${
          record.isBlocked ? "mở khóa" : "khóa"
        } người dùng này?`}
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
      title="Người dùng"
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
        <ShortId label="Mã ngắn" />
        <UserIdentity label="Người dùng" />
        <RoleChip label="Vai trò" />
        <StatusChip label="Trạng thái" />
        <BooleanField source="isBlocked" label="Bị khóa" />
        <DateField source="createdAt" showTime label="Tạo lúc" />
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
        <TextInput source="userName" label="Tên người dùng" validate={[required()]} fullWidth />
        <TextInput source="email" label="Email" validate={[required()]} fullWidth />
        <SelectInput source="role" label="Vai trò" choices={roleChoices} validate={[required()]} />
        <SelectInput source="status" label="Trạng thái" choices={statusChoices} />
        <BooleanField source="isBlocked" label="Bị khóa" />
      </SimpleForm>
    </Edit>
  );
}
