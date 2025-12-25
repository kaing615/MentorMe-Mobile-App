import { Admin, CustomRoutes, Resource } from "react-admin";
import { Route } from "react-router-dom";
import { authProvider } from "./authProvider";
import { dataProvider } from "./dataProvider";
import { Layout } from "./Layout";

import { MyProfile } from "./MyProfile";
import { BookingEdit, BookingList } from "./resources/bookings";
import { ReportEdit, ReportList } from "./resources/reports";
import { UserCreate, UserEdit, UserList } from "./resources/users";
import { PayoutList } from "./resources/payouts";

export default function App() {
  return (
    <Admin
      dataProvider={dataProvider}
      authProvider={authProvider}
      layout={Layout}
    >
      <Resource
        name="users"
        list={UserList}
        edit={UserEdit}
        create={UserCreate}
      />
      <Resource name="bookings" list={BookingList} edit={BookingEdit} />
      <Resource name="reports" list={ReportList} edit={ReportEdit} />

      {/* Resource mới cho payout admin */}
      <Resource
        name="admin/payouts"
        options={{ label: "Mentor Payouts" }}
        list={PayoutList}
      />

      <CustomRoutes>
        <Route path="/my-profile" element={<MyProfile />} />
      </CustomRoutes>
    </Admin>
  );
}
