package com.vrockk.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;

import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vrockk.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class FileUtilsV4 {
    public static final String TAG = FileUtils.class.getCanonicalName();

    public static final String FILES_DIR = "VRockkMedia";
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    public static final String HIDDEN_PREFIX = ".";

    private FileUtilsV4() {

    }

    public static void showFailure(Activity activity, String message) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("OOPS!")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

//    public static void showFileSources(Context context, final OnFileSourceChoiceListener sourceChoiceListener) {
//        Dialog dialog = new Dialog(context);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.dialog_file_source);
//
//        dialog.findViewById(R.id.view_camera).setOnClickListener(view -> {
//            if (sourceChoiceListener != null) {
//                sourceChoiceListener.onCameraChosen();
//            }
//            dialog.dismiss();
//        });
//        dialog.findViewById(R.id.view_gallery).setOnClickListener(view -> {
//            if (sourceChoiceListener != null) {
//                sourceChoiceListener.onGalleryChosen();
//            }
//            dialog.dismiss();
//        });
//        dialog.findViewById(R.id.view_file_manager).setOnClickListener(view -> {
//            if (sourceChoiceListener != null) {
//                sourceChoiceListener.onFileManagerChosen();
//            }
//            dialog.dismiss();
//        });
//
//        dialog.setCancelable(true);
//        dialog.show();
//
//        if (dialog.getWindow() != null)
//            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        resizeDialogToMatchWindow(context, dialog);
//    }

    public static void resizeDialogToMatchWindow(Context context, Dialog dialog) {
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 1.00);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
    }



//    public static ArrayList<FileModel> getFileModels(String[] imagePaths) {
//        ArrayList<FileModel> fileModels = new ArrayList<>();
//        for (int i = 0; i < imagePaths.length; i++) {
//            fileModels.add(new FileModel(new File(imagePaths[i]).getName(), imagePaths[i], ""));
//        }
//        return fileModels;
//    }
//
//    public static FileModel toFileModel(String imagePath) {
//        return new FileModel(new File(imagePath).getName(), imagePath, "");
//    }
//
//    public static FileModel toFileModel(String name, String path, String extra) {
//        return new FileModel(name, path, extra);
//    }
//
//    public static File getDirectory(Directory directory) {
//        return mkDir(new File(
//                mkDir(new File(
//                        Environment.getExternalStorageDirectory()
//                                + "/" + Directory.ROOT.getDirName()))
//                        + "/" + directory.getDirName()));
//    }

    public static File createFile(String fileName, String extension) throws IOException {
        // Create an image file name
        File storageDir = new File(Environment.getExternalStorageDirectory(), FILES_DIR);
        if (!storageDir.exists())
            storageDir.mkdir();
        return new File(storageDir, fileName + extension);
    }

    public static File createTempImageFile(Context context, String fileName) throws IOException {
        // Create an image file name
        File storageDir = new File(context.getCacheDir(), FILES_DIR);
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    public static synchronized File createImageFile(Activity activity, String prefix, byte[] bytes) {
        try {
            File destFile = createImageFile(activity, null, prefix);
            FileOutputStream fileOutputStream = new FileOutputStream(destFile);
            fileOutputStream.write(bytes);
            fileOutputStream.close();
            fileOutputStream.flush();

            return destFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static synchronized File createImageFile(Activity activity, File storageDest, String namePrefix) throws IOException {
        storageDest = storageDest == null ?
                activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES) : storageDest;

        namePrefix = namePrefix == null ? "IMG_" : namePrefix;

        String fileName = namePrefix + fileTimeStamp();
        String fullFileName = fileName + ".jpg";

        File imageFile = new File(storageDest, fullFileName);
        Log.i(TAG, "createImageFile: imageFile = " + imageFile.getAbsolutePath());

        return imageFile;
    }

    public static String getFileRealName(String filepath) {
        String[] fileNameParts = filepath.split("/");
        return fileNameParts[fileNameParts.length - 1];
    }

    /**
     * This method is called to create one, if file does not exist
     *
     * @param directory name for the folder to be created
     */
    public static File mkDir(File directory) {
        if (!directory.exists())
            directory.mkdirs();
        return directory;
    }

//    public static ArrayList<File> getCompressedFiles(Context context, ArrayList<FileModel> fileModels) {
//        ArrayList<File> compressedFiles = new ArrayList<>();
//
//        int fileListSize = fileModels.size();
//        for (int i = 0; i < fileListSize; i++)
//            compressedFiles.add(getCompressedFile(context, fileModels.get(i)));
//
//        return compressedFiles;
//    }
//
//    public static File getCompressedFile(Context context, FileModel fileModel) {
//        return compressFile(context, fileModel.getFilePath());
//    }
//
//    public static ArrayList<File> getCompressedFiles(Context context, List<String> filePaths) {
//        ArrayList<File> compressedFiles = new ArrayList<>();
//
//        int pathListSize = filePaths.size();
//        for (int i = 0; i < pathListSize; i++)
//            compressedFiles.add(compressFile(context, filePaths.get(i)));
//
//        return compressedFiles;
//    }
//
//    public static ArrayList<File> compressFiles(Context context, String[] filePaths) {
//        ArrayList<File> compressedFiles = new ArrayList<>();
//
//        for (String filePath : filePaths)
//            if (!isInvalid(filePath))
//                compressedFiles.add(compressFile(context, filePath));
//
//        return compressedFiles;
//    }

//    public static File compressFile(Context context, String filePath) {
//        File compressedFile = new File(filePath);
//        try {
//            compressedFile = Compressor.getDefault(context).compressToFile(compressedFile);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return compressedFile;
//    }
//
//    public static void createImageThumbnail(Context context, Uri uri, File file) {
//        try {
//            final int THUMBNAIL_SIZE = 64;
//            File compressedFile = compressFile(context, uri.getPath());
//
//            FileInputStream fis = new FileInputStream(compressedFile.getAbsolutePath());
//            Bitmap imageBitmap = BitmapFactory.decodeStream(fis);
//
//            Float width = (float) imageBitmap.getWidth();
//            Float height = (float) imageBitmap.getHeight();
//            Float ratio = width / height;
//            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, (int) (THUMBNAIL_SIZE * ratio), THUMBNAIL_SIZE, false);
//
//            saveThumbnail(file, imageBitmap);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    public static void createVideoThumbnail(Context context, Uri uri, File file) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(uri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
        saveThumbnail(file, bitmap);
    }

//    public static void createDocumentThumbnail(Context context, Uri uri, File file) {
//        int pageNumber = 0;
//        PdfiumCore pdfiumCore = new PdfiumCore(context);
//        try {
//            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(uri, "r");
//            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
//            pdfiumCore.openPage(pdfDocument, pageNumber);
//            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
//            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
//            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
//            pdfiumCore.closeDocument(pdfDocument);
//
//            saveThumbnail(file, bmp);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private static void saveThumbnail(File file, Bitmap bmp) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getTempFileSuffix(String uri) {
        String[] splitUri = uri.split("\\.");
        Log.i(TAG, "getTempFileSuffix: ." + splitUri[splitUri.length - 1]);
        return "." + splitUri[splitUri.length - 1];
    }

    public static String getPlaybackDuration(int milliseconds) {
        String min, sec;
        int seconds = milliseconds / 1000;
        int mins = seconds / 60, secs = seconds % 60;
        if (mins < 10)
            min = "0" + mins;
        else
            min = "" + mins;

        if (secs < 10)
            sec = "0" + secs;
        else
            sec = "" + secs;

        return min + ":" + sec;
    }

    public static String getFileSize(long size) {
        long oneMB = 1024 * 1024, oneKB = 1024, oneMBThousandth = 1000 / oneMB, oneKBThousandth = 1000 / oneKB;
        if (size / oneMB > 0) {
            return size / oneMB + "." + (oneMBThousandth * (size % oneMB)) + " MB";
        } else
            return size / oneKB + "." + (oneKBThousandth * (size % oneKB)) + " KB";
    }

    public static String getFileDataFromIntent(Activity activity, Intent data) {
        return getFileDataFromUri(activity, data.getData());
    }

    public static String getFileDataFromUri(Activity activity, Uri uri) {
        try {
            if (uri != null) {
                activity.grantUriPermission(activity.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String imagePath = getInternalStoragePath(activity, uri);
                if (imagePath == null) {
                    if (isSDCardAvailable(activity)) {
                        imagePath = getExternalStoragePath(activity, uri);
                    }
                    if (imagePath == null) {
                        showFailure(activity, "Unable to fetch file from SDCard." +
                                "\nMove the file into Device Memory and try again.");
                        return null;
                    }
                }
                return imagePath;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getInternalStoragePath(final Context context, final Uri uri) throws IOException {
        context.grantUriPermission(context.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }

            //DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }

            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isSDCardAvailable(Context context) {
        File[] storages = ContextCompat.getExternalFilesDirs(context, null);
        return storages.length > 1 && storages[0] != null && storages[1] != null;
    }

    private static String getSDCardRotPath(Context context) {
        File[] storages = ContextCompat.getExternalFilesDirs(context, null);
        File storageInSDCard = storages[1];
        File removableSDCardAsRootFile = storageInSDCard.getParentFile().getParentFile().getParentFile().getParentFile();
        Log.i(TAG, "getSDCardRotPath: " + String.valueOf(removableSDCardAsRootFile));
        return removableSDCardAsRootFile.getAbsolutePath();
    }

    public static String getExternalStoragePath(Context context, Uri uri) {
        context.grantUriPermission(context.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
            String sdcardPath = getSDCardRotPath(context);
            return sdcardPath.concat("/").concat(split[1]);
        } else
            return null;
    }

    public static String getMimeType(String uri) {
        String[] fileNameParts = new File(uri).getName().split("\\.");

        int filePartsLength = fileNameParts.length;
        Log.i(TAG, "getMimeType: " + fileNameParts[filePartsLength - 1]);
        switch (fileNameParts[filePartsLength - 1]) {
            // text extensions
            case "txt":
                return Mime.txt;
            // image extensions
            case "bmp":
                return Mime.bmp;
            case "cgm":
                return Mime.cgm;
            case "gif":
                return Mime.gif;
            case "jpeg":
                return Mime.jpeg;
            case "jpg":
                return Mime.jpg;
            case "mdi":
                return Mime.mdi;
            case "psd":
                return Mime.psd;
            case "png":
                return Mime.png;
            case "svg":
                return Mime.svg;
            // audio extensions
            case "adp":
                return Mime.adp;
            case "aac":
                return Mime.aac;
            case "mpga":
                return Mime.mpga;
            case "mp4a":
                return Mime.mp4a;
            case "oga":
                return Mime.oga;
            case "wav":
                return Mime.wav;
            case "mp3":
                return Mime.mp3;
            // video extensions
            case "3gp":
                return Mime.a3gp;
            case "3g2":
                return Mime.a3g2;
            case "avi":
                return Mime.avi;
            case "xlv":
                return Mime.xlv;
            case "m4v":
                return Mime.m4v;
            case "mp4":
                return Mime.mp4;
            // documents extension
            case "rar":
                return Mime.rar;
            case "zip":
                return Mime.zip;
            case "pdf":
                return Mime.pdf;
            case "dvi":
                return Mime.dvi;
            case "karbon":
                return Mime.karbon;
            case "mdb":
                return Mime.mdb;
            case "xls":
                return Mime.xls;
            case "pptx":
                return Mime.pptx;
            case "docx":
                return Mime.docx;
            case "ppt":
                return Mime.ppt;
            case "doc":
                return Mime.doc;
            case "oxt":
                return Mime.oxt;
            default:
                return "file/*";
        }
    }

    public static String getFileCategory(String uri) {
        String mimeType = getMimeType(uri);
        Log.i(TAG, "getFileCategory. " + mimeType.split("/")[0]);
        return mimeType.split("/")[0];
    }

//    public static boolean deleteFileFromStorage(ArrayList<File> files) {
//        boolean deleted = true;
//        try {
//            for (File file : files)
//                deleted = deleted && deleteFileFromStorage(file);
//        } catch (Exception e) {
//            Log.e(FileManager.TAG, "deleteFileFromStorage: " + e.getMessage());
//        }
//        return deleted;
//    }
//
//    public static boolean deleteFileFromStorage(File file) {
//        try {
//            return file.delete();
//        } catch (Exception e) {
//            Log.e(FileManager.TAG, "deleteFileFromStorage: " + e.getMessage());
//            return false;
//        }
//    }

    /**
     * Creates the specified <code>toFile</code> as a byte for byte copy of the
     * <code>fromFile</code>. If <code>toFile</code> already exists, then it
     * will be replaced with a copy of <code>fromFile</code>. The name and path
     * of <code>toFile</code> will be that of <code>toFile</code>.<br/>
     * <br/>
     * <i> Note: <code>fromFile</code> and <code>toFile</code> will be closed by
     * this function.</i>
     *
     * @param fromFile - FileInputStream for the file to copy from.
     * @param toFile   - FileInputStream for the file to copy to.
     */
    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

    public static boolean isInvalid(String text) {
        return !TextUtils.isEmpty(text);
    }

    public static String fileTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        String todayNow = sdf.format(new Date());
        todayNow = todayNow.replace("/", "");
        todayNow = todayNow.replace(" ", "_");
        todayNow = todayNow.replace(":", "");

        return todayNow;
    }

    public static final class Mime {
        // Text Mime
        public static final String txt = "text/plain";

        // Image Mime
        public static final String bmp = "image/bmp";
        public static final String cgm = "image/cgm";
        public static final String gif = "image/gif";
        public static final String jpeg = "image/jpeg";
        public static final String jpg = "image/jpg";
        public static final String mdi = "image/vnd.ms-modi";
        public static final String psd = "image/vnd.adobe.photoshop";
        public static final String png = "image/png";
        public static final String svg = "image/svg";

        // Audio Mime
        public static final String adp = "audio/adpcm";
        public static final String aac = "audio/x-aac";
        public static final String mpga = "audio/mpeg";
        public static final String mp4a = "audio/mp4";
        public static final String oga = "audio/ogg";
        public static final String wav = "audio/x-wav";
        public static final String mp3 = "audio/mp3";

        // Video Mime
        public static final String a3gp = "video/3gpp";
        public static final String a3g2 = "video/3gpp2";
        public static final String avi = "video/x-msvideo";
        public static final String xlv = "video/x-flv";
        public static final String m4v = "video/x-m4v";
        public static final String mp4 = "video/mp4";

        // Document Mime
        public static final String pdf = "application/pdf";
        public static final String dvi = "application/x-dvi";
        public static final String karbon = "application/vnd.kdee.karbon";
        public static final String mdb = "application/x-msaccess";
        public static final String xls = "application/vnd.ms-excel";
        public static final String pptx = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        public static final String docx = "application/vnd.openxmlformats-officedocument.spreadsheet.template";
        public static final String ppt = "application/vnd.ms-powerpoint";
        public static final String doc = "application/vnd.ms-word";
        public static final String oxt = "application/vnd.openofficeorg.extension";
        public static final String rar = "application/x-rar-compressed";
        public static final String zip = "application/zip";

        // For all
        public static final String all = "file/*";

        private Mime() {
        }
    }

    public static final class Extension {
        // Text Extensions
        public static final String txt = "txt";

        private Extension() {
        }
    }
}
