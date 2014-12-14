package com.jparkie.aizoban.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DiskUtils {
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static File saveInputStreamToDirectory(InputStream inputStream, String directory, String name) throws IOException {
        File fileDirectory = new File(directory);
        if (!fileDirectory.exists()) {
            if (!fileDirectory.mkdirs()) {
                throw new IOException("Failed Creating  Directory");
            }
        }

        File writeFile = new File(fileDirectory, name);
        if (writeFile.exists()) {
            if (writeFile.delete()) {
                writeFile = new File(fileDirectory, name);
            } else {
                throw new IOException("Failed Deleting Existing File for Overwrite");
            }
        }

        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(writeFile);

            byte[] fileBuffer= new byte[1024];
            for (int counter = 0; counter != -1; counter = inputStream.read(fileBuffer, 0, 1024)) {
                outputStream.write(fileBuffer, 0, counter);
            }

            outputStream.flush();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }

        return writeFile;
    }

    public static void deleteFiles(File inputFile) {
        if (inputFile.isDirectory()) {
            for (File childFile : inputFile.listFiles()) {
                deleteFiles(childFile);
            }
        }

        inputFile.delete();
    }
}
