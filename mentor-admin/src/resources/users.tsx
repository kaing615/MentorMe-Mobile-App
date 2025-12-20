import * as React from "react";
import {
  BooleanField,
  BooleanInput,
  Button,
  Confirm,
  Datagrid,
  DateField,
  Edit,
  EditButton,
  EmailField,
  List,
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
  { id: "admin", name: "admin" },
  { id: "moderator", name: "moderator" },
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
        disabled={record.role === "admin" && nextBlocked} // tùy bạn
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
      </Datagrid>
    </List>
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
