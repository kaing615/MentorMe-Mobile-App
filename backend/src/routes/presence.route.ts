// filepath: backend/src/routes/presence.route.ts
import { Router } from "express";
import { lookupPresence, pingPresence } from "../controllers/presence.controller";
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

/**
 * @swagger
 * /presence/lookup:
 *   post:
 *     summary: Lookup online status for users
 *     tags: [Presence]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               userIds:
 *                 type: array
 *                 items:
 *                   type: string
 *     responses:
 *       200:
 *         description: Presence lookup successful
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
 *                     onlineUserIds:
 *                       type: array
 *                       items:
 *                         type: string
 */
router.post("/lookup", auth, lookupPresence);

export default router;

