package com.stupidtree.hita.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.WorkerThread;
import androidx.core.app.ActivityCompat;

import com.stupidtree.hita.timetable.CurriculumCreator;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.HTime;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class FileOperator {
    private static final int CHOOSE_FILE_CODE = 0;


    public static boolean verifyStoragePermissions(Activity activity) {
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"};
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static final String TAG1 = "FileChoose";


    private static boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @WorkerThread
    public static boolean saveByteImageToFile(String path, Bitmap bitmap) {
        String finalName;
        int index = path.lastIndexOf(".");
        if (index < 0 || index >= path.length()) { //没有扩展名
            finalName = path + "x.jpg";
        } else { //判断扩展名
            String name = path.substring(index);
            if (!name.equals(".jpeg") && !name.equals(".jpg")) {
                if (name.length() < 5) {
                    String replaced = path.replaceAll(name, "");
                    finalName = replaced + ".jpg";
                } else {
                    finalName = path + ".jpg";
                }

            } else {
                finalName = path;
            }
        }

        BufferedOutputStream os;
        try {
            File file = new File(finalName);  //新建图片
            if (!file.getParentFile().exists()) {  //如果文件夹不存在，创建文件夹
                file.getParentFile().mkdirs();
            }
            if (file.exists()) file.delete();
            file.createNewFile();  //创建图片文件
            os = new BufferedOutputStream(new FileOutputStream(file, false));

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);  //图片存成png格式。
            os.close();  //关闭流
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // 调用系统文件管理器
    public static void chooseFile(Activity activity, File path) {
        FileOperator.verifyStoragePermissions(activity);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);
        try {
            activity.startActivityForResult(Intent.createChooser(intent, "Choose File"), CHOOSE_FILE_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "打开文件管理器失败", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

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
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    @WorkerThread
    public static List<CurriculumCreator> loadCurriculumFromExcel(File location, Calendar date) throws Exception {
        List<CurriculumCreator> result = new ArrayList<>();
        Workbook workbook = Workbook.getWorkbook(location);
        Sheet[] sheets = workbook.getSheets();
        for (Sheet sheet : sheets) {
            String sheetName = sheet.getName();
            List<Map<String, String>> ccData = new ArrayList<>();
            CurriculumCreator cc = CurriculumCreator.create(UUID.randomUUID().toString(), sheetName, date);
            for (int j = 1; j < sheet.getRows(); j++) {
                Map<String, String> m = new HashMap<>();
                Cell[] sheetRow = sheet.getRow(j);
                String name = sheetRow[0].getContents();
                String classroom = sheetRow[1].getContents();
                String teacher = sheetRow[2].getContents();
                String dow = sheetRow[3].getContents();
                String weekText = sheetRow[4].getContents();
                String time = sheetRow[5].getContents();
                m.put("name", name);
                m.put("classroom", classroom);
                m.put("teacher", teacher);
                m.put("dow", dow);
                if (!TextUtils.isEmpty(weekText)) {
                    StringBuilder weeks = new StringBuilder();
                    List<Integer> weekI = new ArrayList<>();
                    weekText = weekText.replaceAll("，", ",").replaceAll("、", ",");
                    for (String wk : weekText.split(",")) {
                        boolean pairW = false;
                        boolean singW = false;
                        if (wk.contains("单")) {
                            singW = true;
                            wk = wk.replaceAll("单", "").replaceAll("周", "");
                        } else if (wk.contains("双")) {
                            pairW = true;
                            wk = wk.replaceAll("双", "").replaceAll("周", "");
                        } else wk = wk.replaceAll("周", "");
                        if (wk.contains("-")) {
                            String[] ft = wk.split("-");
                            int from = Integer.parseInt(ft[0]);
                            int to = Integer.parseInt(ft[1]);
                            for (int i = from; i <= to; i++) {
                                if (pairW && i % 2 != 0 || singW && i % 2 == 0) continue;
                                if (!weekI.contains(i)) weekI.add(i);
                                //weeks.append(i+"").append(",");
                            }
                        } else if (!weekI.contains(Integer.parseInt(wk)))
                            weekI.add(Integer.parseInt(wk));
                    }
                    for (int i = 0; i < weekI.size(); i++) {
                        weeks.append(weekI.get(i) + "").append(i == weekI.size() - 1 ? "" : ",");
                    }
                    m.put("weeks", weeks.toString());
                }
                time = time.replaceAll("~", "-");
                if (time.contains(":") || time.contains("：")) {
                    time = time.replaceAll("：", ":");
                    HTime from = new HTime(time.split("-")[0]);
                    HTime to = new HTime(time.split("-")[1]);
                    int begin = TimetableCore.getNumberAtTime(from);
                    int last = TimetableCore.getNumberAtTime(to) - begin + 1;
                    m.put("begin", String.valueOf(begin));
                    m.put("last", String.valueOf(last));
                } else {
                    String[] spcf = time.split("-");
                    m.put("begin", String.valueOf(Integer.parseInt(spcf[0])));
                    m.put("last", String.valueOf(Integer.parseInt(spcf[1]) - Integer.parseInt(spcf[0]) + 1));
                }
                ccData.add(m);
            }
            result.add(cc.loadCourse(ccData));
        }
        return result;
    }


}
