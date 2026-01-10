import { Admin, CustomRoutes, Resource } from "react-admin";
import { Route } from "react-router-dom";
import { authProvider } from "./authProvider";
import { dataProvider } from "./dataProvider";
import { Layout } from "./Layout";

import {
  UserList,
  UserEdit,
  BookingList,
  BookingEdit,
  SessionList,
  ReportList,
  PayoutList,
  TopUpList,
  MentorApplicationList,
  MentorApplicationEdit,
  WebhookPage,
} from "./resources";

import { MyProfile } from "./MyProfile";

export default function App() {
  return (
    <Admin
      dataProvider={dataProvider}
      authProvider={authProvider}
      layout={Layout}
      requireAuth
      requireAuth
    >
      <Resource name="users" list={UserList} edit={UserEdit} />

      <Resource name="users" list={UserList} edit={UserEdit} />

      <Resource
        name="admin/bookings"
        options={{ label: "Bookings" }}
        list={BookingList}
        edit={BookingEdit}
      />

      <Resource
        name="admin/sessions"
        options={{ label: "Sessions" }}
        list={SessionList}
      />

      {/* Moderation */}
      <Resource name="reports" list={ReportList} />

      {/* Moderation */}
      <Resource name="reports" list={ReportList} />

      {/* Admin only */}
      {/* Admin only */}
      <Resource
        name="admin/payouts"
        options={{ label: "Mentor Payouts" }}
        list={PayoutList}
      />

      <Resource
        name="admin/webhook-logs"
        options={{ label: "Webhook Logs" }}
        list={WebhookPage}
      />

      <Resource
        name="admin/topup-intents"
        options={{ label: "TopUp Intents" }}
        list={TopUpList}
      />

      <Resource
        name="mentor-applications"
        options={{ label: "Mentor Applications" }}
        list={MentorApplicationList}
        edit={MentorApplicationEdit}
      />

      <CustomRoutes>
        <Route path="/my-profile" element={<MyProfile />} />
      </CustomRoutes>
    </Admin>
  );
}
