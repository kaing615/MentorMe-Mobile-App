// filepath: backend/src/routes/home.route.ts
import { Router } from "express";
import { getHomeStats } from "../controllers/home.controller";

const router = Router();

/**
 * @swagger
 * /home/stats:
 *   get:
 *     summary: Get home screen statistics
 *     tags: [Home]
 *     responses:
 *       200:
 *         description: Successfully retrieved statistics
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                 data:
 *                   type: object
 *                   properties:
 *                     mentorCount:
 *                       type: number
 *                     sessionCount:
 *                       type: number
 *                     avgRating:
 *                       type: number
 *                     onlineCount:
 *                       type: number
 */
router.get("/stats", getHomeStats);

export default router;

