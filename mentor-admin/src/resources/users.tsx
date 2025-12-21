import * as React from "react";
import {
  BooleanField,
  BooleanInput,
  Button,
  Confirm,
  Create,
  Datagrid,
  DateField,
  DeleteButton,
  Edit,
  EditButton,
  EmailField,
  List,
  PasswordInput,
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

type User = {
  id: string | number;
  name?: string;
  email?: string;
  role?: "admin" | "moderator" | "mentor" | "mentee";
  isBlocked?: boolean;
  createdAt?: string;
};

const roleChoices = [
  { id: "root", name: "root" },
  { id: "admin", name: "admin" },
  { id: "mentor", name: "mentor" },
  { id: "mentee", name: "mentee" },
];

const UserFilters = [
  <TextInput key="q" source="q" label="Search" alwaysOn />,
  <SelectInput key="role" source="role" label="Role" choices={roleChoices} />,
  <SelectInput
    key="isBlocked"
    source="isBlocked"
    label="Blocked"
    choices={[
      { id: "true", name: "Blocked" },
      { id: "false", name: "Active" },
    ]}
  />,
];

function BlockToggleButton() {
  const record = useRecordContext<User>();
  const dataProvider = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();

  const [open, setOpen] = React.useState(false);

  if (!record) return null;

  const nextBlocked = !Boolean(record.isBlocked);
  const label = nextBlocked ? "Lock" : "Unlock";

  const onConfirm = async () => {
    try {
      await dataProvider.update("users", {
        id: record.id,
        data: { isBlocked: nextBlocked },
        previousData: record,
      });
      notify(`User updated`, { type: "success" });
      refresh();
    } catch (e: any) {
      notify(e?.message || "Update failed", { type: "error" });
    } finally {
      setOpen(false);
    }
  };

  return (
    <>
      <Button
        label={label}
        onClick={() => setOpen(true)}
        disabled={record.role === "admin" && nextBlocked}
      />
      <Confirm
        isOpen={open}
        title={`${label} user`}
        content={`Are you sure you want to ${label.toLowerCase()} this user?`}
        onConfirm={onConfirm}
        onClose={() => setOpen(false)}
      />
    </>
  );
}

function ChangePasswordButton() {
  const record = useRecordContext<User>();
  const dataProvider = useDataProvider();
  const notify = useNotify();

  const [open, setOpen] = React.useState(false);
  const [password, setPassword] = React.useState("");
  const [confirmPassword, setConfirmPassword] = React.useState("");
  const [loading, setLoading] = React.useState(false);

  if (!record) return null;

  const onConfirm = async () => {
    if (!password || password.length < 6) {
      notify("Password must be at least 6 characters", { type: "error" });
      return;
    }

    if (password !== confirmPassword) {
      notify("Passwords do not match", { type: "error" });
      return;
    }

    setLoading(true);
    try {
      await fetch(`${import.meta.env.VITE_API_URL}/users/${record.id}/password`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("access_token")}`,
        },
        body: JSON.stringify({ password }),
      });
      notify("Password changed successfully", { type: "success" });
      setOpen(false);
      setPassword("");
      setConfirmPassword("");
    } catch (e: any) {
      notify(e?.message || "Failed to change password", { type: "error" });
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Button label="Change Password" onClick={() => setOpen(true)} />
      <Confirm
        isOpen={open}
        loading={loading}
        title="Change Password"
        content={
          <div style={{ padding: "16px 0" }}>
            <input
              type="password"
              placeholder="New password (min 6 characters)"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              style={{
                width: "100%",
                padding: "8px",
                fontSize: "14px",
                border: "1px solid #ccc",
                borderRadius: "4px",
                marginBottom: "12px",
              }}
            />
            <input
              type="password"
              placeholder="Confirm new password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              style={{
                width: "100%",
                padding: "8px",
                fontSize: "14px",
                border: "1px solid #ccc",
                borderRadius: "4px",
              }}
            />
          </div>
        }
        onConfirm={onConfirm}
        onClose={() => {
          setOpen(false);
          setPassword("");
          setConfirmPassword("");
        }}
      />
    </>
  );
}

export function UserList() {
  return (
    <List filters={UserFilters} sort={{ field: "createdAt", order: "DESC" }}>
      <Datagrid rowClick={false}>
        <TextField source="id" />
        <TextField source="name" />
        <EmailField source="email" />
        <TextField source="role" />
        <BooleanField source="isBlocked" label="Blocked" />
        <DateField source="createdAt" showTime />
        <EditButton />
        <BlockToggleButton />
        <ChangePasswordButton />
        <DeleteButton />
      </Datagrid>
    </List>
  );
}

export function UserCreate() {
  return (
    <Create>
      <SimpleForm>
        <RaTextInput source="email" validate={[required()]} fullWidth type="email" />
        <RaTextInput source="userName" validate={[required()]} fullWidth />
        <RaTextInput source="name" fullWidth helperText="Optional - defaults to userName" />
        <RaSelectInput
          source="role"
          choices={roleChoices}
          validate={[required()]}
          defaultValue="mentee"
        />
        <PasswordInput 
          source="password" 
          fullWidth 
          helperText="Optional - auto-generated if empty" 
        />
      </SimpleForm>
    </Create>
  );
}

export function UserEdit() {
  return (
    <Edit>
      <SimpleForm>
        <RaTextInput source="name" validate={[required()]} fullWidth />
        <RaTextInput source="email" validate={[required()]} fullWidth />
        <RaSelectInput
          source="role"
          choices={roleChoices}
          validate={[required()]}
        />
        <BooleanInput source="isBlocked" label="Blocked" />
      </SimpleForm>
    </Edit>
  );
}
