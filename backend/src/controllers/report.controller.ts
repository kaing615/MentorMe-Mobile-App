import { Request, Response } from "express";
import { asyncHandler } from "../handlers/async.handler";
import responseHandler from "../handlers/response.handler";
import Report from "../models/report.model";

function toReportResponse(report: any) {
  return {
    id: String(report._id),
    type: report.type ?? "",
    status: report.status,
    targetType: report.targetType,
    targetId: report.targetId,
    reporterId: report.reporter ? String(report.reporter) : undefined,
    note: report.note ?? "",
    createdAt: report.createdAt?.toISOString?.() ?? report.createdAt ?? null,
    updatedAt: report.updatedAt?.toISOString?.() ?? report.updatedAt ?? null,
  };
}

export const getAllReports = asyncHandler(async (req: Request, res: Response) => {
  const {
    filter = "{}",
    range = "[0,9]",
    sort = '["createdAt","DESC"]',
  } = req.query;

  const filterObj = JSON.parse(filter as string);
  const [start, end] = JSON.parse(range as string);
  const [sortField, sortOrder] = JSON.parse(sort as string);

  const query: any = {};

  if (filterObj.status) query.status = filterObj.status;
  if (filterObj.targetType) query.targetType = filterObj.targetType;
  if (filterObj.reporterId) query.reporter = filterObj.reporterId;

  if (filterObj.q) {
    const q = String(filterObj.q).trim();
    if (q) {
      query.$or = [
        { type: { $regex: q, $options: "i" } },
        { note: { $regex: q, $options: "i" } },
        { targetId: { $regex: q, $options: "i" } },
      ];
    }
  }

  const total = await Report.countDocuments(query);
  const reports = await Report.find(query)
    .sort({ [sortField]: sortOrder === "DESC" ? -1 : 1 })
    .skip(start)
    .limit(end - start + 1)
    .lean();

  res.set("Content-Range", `reports ${start}-${end}/${total}`);
  res.set("Access-Control-Expose-Headers", "Content-Range");
  return res.json(reports.map(toReportResponse));
});

export const getReportById = asyncHandler(async (req: Request, res: Response) => {
  const report = await Report.findById(req.params.id).lean();
  if (!report) return responseHandler.notFound(res, null, "Report not found");
  return res.json(toReportResponse(report));
});

export const updateReport = asyncHandler(async (req: Request, res: Response) => {
  const { status, note } = req.body ?? {};

  const updates: any = {};
  if (status !== undefined) updates.status = status;
  if (note !== undefined) updates.note = note;

  const report = await Report.findByIdAndUpdate(req.params.id, updates, {
    new: true,
  }).lean();

  if (!report) return responseHandler.notFound(res, null, "Report not found");
  return res.json(toReportResponse(report));
});

export default {
  getAllReports,
  getReportById,
  updateReport,
};
