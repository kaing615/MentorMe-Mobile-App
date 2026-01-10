import axios from "axios";
import {
  v2 as cloudinary,
  UploadApiOptions,
  UploadApiResponse
} from "cloudinary";
import dotenv from "dotenv";
import FormData from "form-data";

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
  cloudinary.config({ 
    secure: true,
    api_proxy: process.env.CLOUDINARY_API_PROXY, // Optional proxy
  });
} else {
  cloudinary.config({
    cloud_name: process.env.CLOUDINARY_CLOUD_NAME!,
    api_key: process.env.CLOUDINARY_API_KEY!,
    api_secret: process.env.CLOUDINARY_API_SECRET!,
    secure: true,
    api_proxy: process.env.CLOUDINARY_API_PROXY, // Optional proxy
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
    chunk_size: 6000000, // 6MB chunks
    ...options,
  };

  if (Buffer.isBuffer(file)) {
    // Use base64 upload for buffers
    const base64Data = `data:image/jpeg;base64,${file.toString('base64')}`;
    
    try {
      const result = await cloudinary.uploader.upload(base64Data, opts);
      console.log('[Cloudinary] Image upload completed');
      return result as UploadApiResponse;
    } catch (error) {
      console.error('[Cloudinary] Image upload error:', error);
      throw error;
    }
  }

  return (await cloudinary.uploader.upload(file, opts)) as UploadApiResponse;
}

async function uploadFile(
  file: string | Buffer,
  options: UploadApiOptions = {}
): Promise<UploadResult> {
  console.log('[Cloudinary] uploadFile called with options:', {
    folder: options.folder,
    resource_type: options.resource_type,
    bufferSize: Buffer.isBuffer(file) ? file.length : 'N/A',
  });

  if (Buffer.isBuffer(file)) {
    // Use direct HTTP upload with axios (more reliable than SDK)
    try {
      console.log('[Cloudinary] Using direct HTTP upload');
      
      const cloudName = process.env.CLOUDINARY_URL?.match(/cloudinary:\/\/.*@(.+)$/)?.[1] || process.env.CLOUDINARY_CLOUD_NAME;
      
      if (!cloudName) {
        throw new Error('Missing Cloudinary cloud name');
      }
      
      // Try unsigned upload first (faster, no signature required)
      const formData = new FormData();
      formData.append('file', file, { filename: 'upload.bin' });
      formData.append('upload_preset', 'ml_default'); // Use default unsigned preset
      if (options.folder) formData.append('folder', options.folder);
      if (options.public_id) formData.append('public_id', options.public_id);
      
      const resourceType = options.resource_type || 'auto';
      const uploadUrl = `https://api.cloudinary.com/v1_1/${cloudName}/${resourceType}/upload`;
      
      console.log('[Cloudinary] Uploading to:', uploadUrl);
      console.log('[Cloudinary] Resource type:', resourceType);
      const startTime = Date.now();
      
      const response = await axios.post(uploadUrl, formData, {
        headers: formData.getHeaders(),
        timeout: 120000, // 2 minutes
        maxContentLength: Infinity,
        maxBodyLength: Infinity,
        onUploadProgress: (progressEvent) => {
          const percentCompleted = progressEvent.total 
            ? Math.round((progressEvent.loaded * 100) / progressEvent.total)
            : 0;
          if (percentCompleted % 25 === 0) {
            console.log(`[Cloudinary] Upload progress: ${percentCompleted}%`);
          }
        },
      });
      
      const duration = Date.now() - startTime;
      console.log('[Cloudinary] Upload completed in', duration, 'ms');
      
      return response.data as UploadApiResponse;
    } catch (error: any) {
      console.error('[Cloudinary] Upload error:', {
        message: error.message,
        code: error.code,
        timeout: error.code === 'ECONNABORTED',
      });
      
      if (error.response) {
        console.error('[Cloudinary] Response status:', error.response.status);
        console.error('[Cloudinary] Response data:', error.response.data);
      }
      
      // If unsigned upload fails, fallback to signed upload
      if (error.response?.status === 400 || error.code === 'ECONNABORTED') {
        console.log('[Cloudinary] Trying signed upload as fallback...');
        
        try {
          const apiKey = process.env.CLOUDINARY_URL?.match(/cloudinary:\/\/(\d+):/)?.[1] || process.env.CLOUDINARY_API_KEY;
          const apiSecret = process.env.CLOUDINARY_URL?.match(/cloudinary:\/\/\d+:([^@]+)@/)?.[1] || process.env.CLOUDINARY_API_SECRET;
          
          if (!apiKey || !apiSecret) {
            throw new Error('Missing Cloudinary API credentials');
          }
          
          const opts: UploadApiOptions = {
            resource_type: options.resource_type || "auto",
            folder: options.folder ?? "mentor-me-mobile-app/chat-files",
            public_id: options.public_id,
            timeout: 120000,
          };
          
          const base64Data = `data:application/octet-stream;base64,${file.toString('base64')}`;
          const result = await cloudinary.uploader.upload(base64Data, opts);
          console.log('[Cloudinary] Signed upload succeeded');
          return result as UploadApiResponse;
        } catch (fallbackError: any) {
          console.error('[Cloudinary] Signed upload also failed:', fallbackError.message);
          throw fallbackError;
        }
      }
      
      throw error;
    }
  }
  
  const opts: UploadApiOptions = {
    resource_type: "auto",
    folder: options.folder ?? "mentor-me-mobile-app/chat-files",
    ...options,
  };

  return (await cloudinary.uploader.upload(file, opts)) as UploadApiResponse;
}

async function deleteAsset(
  public_id: string,
  resource_type: "image" | "video" | "raw" | "auto" = "image",
  invalidate = true
) {
  return cloudinary.uploader.destroy(public_id, { resource_type, invalidate });
}

function generateSignature(paramsToSign: Record<string, any>, apiSecret: string): string {
  return cloudinary.utils.api_sign_request(paramsToSign, apiSecret);
}

export default {
  uploadImage,
  uploadFile,
  deleteAsset,
  generateSignature,
};
