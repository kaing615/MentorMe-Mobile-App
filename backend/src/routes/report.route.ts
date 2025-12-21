import { Router } from "express";
import reportController from "../controllers/report.controller";

const router = Router();

router.get("/", reportController.getAllReports);
router.get("/:id", reportController.getReportById);
router.put("/:id", reportController.updateReport);

export default router;
