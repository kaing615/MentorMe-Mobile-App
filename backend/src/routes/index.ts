import { Router } from "express";
import userRoute from "./user.route";
import profileRoute from "./profile.route";

const router = Router();

router.use("/auth", userRoute);
router.use("/profile", profileRoute);

export default router;
