import { Request, Response } from "express";
import { asyncHandler } from "../handlers/async.handler";

export const getAllReports = asyncHandler(async (req: Request, res: Response) => {
  const { filter = '{}', range = '[0,9]', sort = '["createdAt","DESC"]' } = req.query;
  
  // Tạm thời trả về mảng rỗng vì chưa có model Report
  const reports: any[] = [];
  const total = 0;
  
  const [start, end] = JSON.parse(range as string);
  
  res.set('Content-Range', `reports ${start}-${end}/${total}`);
  res.set('Access-Control-Expose-Headers', 'Content-Range');
  return res.json(reports);
});

export const getReportById = asyncHandler(async (req: Request, res: Response) => {
  return res.json({ id: req.params.id, status: "pending" });
});

export const updateReport = asyncHandler(async (req: Request, res: Response) => {
  return res.json({ id: req.params.id, ...req.body });
});

export default {
  getAllReports,
  getReportById,
  updateReport,
};
