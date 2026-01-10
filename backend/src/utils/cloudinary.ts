import dotenv from "dotenv";
import {
  v2 as cloudinary,
  UploadApiResponse,
  UploadApiErrorResponse,
  UploadApiOptions,
} from "cloudinary";

dotenv.config();

if (
  !process.env.CLOUDINARY_URL &&
  (!process.env.CLOUDINARY_CLOUD_NAME ||
    !process.env.CLOUDINARY_API_KEY ||
    !process.env.CLOUDINARY_API_SECRET)
) {
  throw new Error(
    "Missing Cloudinary config: set CLOUDINARY_URL or CLOUDINARY_CLOUD_NAME/API_KEY/API_SECRET"
  );
}

if (process.env.CLOUDINARY_URL) {
  cloudinary.config({ secure: true });
} else {
  cloudinary.config({
    cloud_name: process.env.CLOUDINARY_CLOUD_NAME!,
    api_key: process.env.CLOUDINARY_API_KEY!,
    api_secret: process.env.CLOUDINARY_API_SECRET!,
    secure: true,
  });
}

type UploadResult = UploadApiResponse;

async function uploadImage(
  file: string | Buffer,
  options: UploadApiOptions = {}
): Promise<UploadResult> {
  const opts: UploadApiOptions = {
    resource_type: "image",
    folder: options.folder ?? "mentor-me-mobile-app",
    overwrite: options.overwrite ?? false,
    unique_filename: options.unique_filename ?? true,
    use_filename: options.use_filename ?? false,
    ...options,
  };

  if (Buffer.isBuffer(file)) {
    return new Promise((resolve, reject) => {
      const stream = cloudinary.uploader.upload_stream(
        opts,
        (err?: UploadApiErrorResponse, res?: UploadApiResponse) => {
          if (err) return reject(err);
          resolve(res as UploadApiResponse);
        }
      );
      stream.end(file);
    });
  }

  return (await cloudinary.uploader.upload(file, opts)) as UploadApiResponse;
}

async function uploadFile(
  file: string | Buffer,
  options: UploadApiOptions = {}
): Promise<UploadResult> {
  const opts: UploadApiOptions = {
    resource_type: "auto",
    folder: options.folder ?? "mentor-me-mobile-app/chat-files",
    overwrite: options.overwrite ?? false,
    unique_filename: options.unique_filename ?? true,
    use_filename: options.use_filename ?? true,
    ...options,
  };

  if (Buffer.isBuffer(file)) {
    return new Promise((resolve, reject) => {
      const stream = cloudinary.uploader.upload_stream(
        opts,
        (err?: UploadApiErrorResponse, res?: UploadApiResponse) => {
          if (err) return reject(err);
          resolve(res as UploadApiResponse);
        }
      );
      stream.end(file);
    });
  }

  return (await cloudinary.uploader.upload(file, opts)) as UploadApiResponse;
}

async function deleteAsset(
  public_id: string,
  resource_type: "image" | "video" | "raw" | "auto" = "image",
  invalidate = true
) {
  return cloudinary.uploader.destroy(public_id, { resource_type, invalidate });
}

export default {
  uploadImage,
  uploadFile,
  deleteAsset,
};
