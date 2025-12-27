// filepath: backend/src/routes/presence.route.ts
import { Router } from "express";
import { pingPresence } from "../controllers/presence.controller";
import { auth } from "../middlewares/auth.middleware";

const router = Router();

/**
 * @swagger
 * /presence/ping:
 *   post:
 *     summary: Update user online presence
 *     tags: [Presence]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Presence updated successfully
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
 *                     userId:
 *                       type: string
 *                     expiresIn:
 *                       type: number
 */
router.post("/ping", auth, pingPresence);

export default router;

