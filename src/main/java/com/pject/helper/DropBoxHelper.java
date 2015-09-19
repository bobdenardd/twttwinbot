package com.pject.helper;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWriteMode;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * DropBoxHelper - Short description of the class
 *
 * @author Camille
 *         Last: 15/09/15 16:13
 * @version $Id$
 */
public class DropBoxHelper {

    private static final Logger LOGGER = Logger.getLogger(DropBoxHelper.class);

    private static final String SEPARATOR = "/";

    private static DbxClient client;
    private static String authToken;

    public static void init(String authToken_) {
        authToken = authToken_;
    }

    public static void downloadFile(String remoteFile, File localFile) {
        LOGGER.info("Downloading file " + remoteFile + " to " + localFile.getAbsolutePath());
        initIfNeeded();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(localFile);
            client.getFile(remoteFile, null, outputStream);
            LOGGER.info("Downloaded file " + remoteFile + " to " + localFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Could not download file " + remoteFile + " to " + localFile.getAbsolutePath() + ": " + e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Could not close strem: " + e.getMessage());
                }
            }
        }
    }

    public static void uploadFile(String remoteFile, File localFile) {
        LOGGER.info("Uploading file " + localFile.getAbsolutePath() + " to " + remoteFile);
        initIfNeeded();
        try {
            FileInputStream inputStream = new FileInputStream(localFile);
            try {
                client.uploadFile(remoteFile, DbxWriteMode.force(), localFile.length(), inputStream);
                LOGGER.info("Uploaded file " + localFile.getAbsolutePath() + " to " + remoteFile);
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            LOGGER.error("Could not upload file " + localFile.getAbsolutePath() + " to " + remoteFile + ": " + e.getMessage());
        }
    }

    public static String getRemoteFile(String... files) {
        StringBuilder builder = new StringBuilder();
        for (String file : files) {
            builder.append(file).append(SEPARATOR);
        }
        return StringUtils.stripEnd(builder.toString(), SEPARATOR);
    }

    private static void initIfNeeded() {
        if (client == null) {
            client = new DbxClient(new DbxRequestConfig("CoinUrl/1.0", Locale.getDefault().toString()), authToken);
        }
    }

}
