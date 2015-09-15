package com.pject.helper;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWriteMode;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
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

    public static final String SEPARATOR = "/";

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

    public static boolean moveFile(String remoteSource, String remoteDestination) {
        LOGGER.info("Moving file " + remoteSource + " to " + remoteDestination);
        initIfNeeded();
        try {
            client.move(remoteSource, remoteDestination);
            LOGGER.info("Moved file " + remoteSource + " to " + remoteDestination);
            return true;
        } catch (Exception e) {
            LOGGER.error("Could not move file " + remoteSource + " to " + remoteDestination + ": " + e.getMessage());
        }
        return false;
    }

    public static List<File> synchronizeLocalFolderWithRemote(File localFolder, String remoteFolder) {
        List<File> result = Lists.newArrayList();
        if (!localFolder.exists()) {
            LOGGER.info("Creating local directory " + localFolder.getAbsolutePath() + ": " + (localFolder.mkdirs() ? "success" : "failure"));
        } else {
            LOGGER.info("Local directory " + localFolder.getAbsolutePath() + " already exists");
        }

        List<DropBoxFile> remoteFiles = listRemoteDFilesInDir(remoteFolder, true);
        if (remoteFiles != null && remoteFiles.size() > 0) {
            List<String> existing = Lists.newArrayList(localFolder.list(new FilenameFilter() {
                //@Override
                public boolean accept(File dir, String name) {
                    return new File(dir.getAbsolutePath() + File.separator + name).isFile();
                }
            }));
            for (DropBoxFile dFile : remoteFiles) {
                String remoteFileName = dFile.getRemoteFile().substring(dFile.getRemoteFile().lastIndexOf(DropBoxHelper.SEPARATOR) + 1);
                File localFile = new File(localFolder.getAbsolutePath() + File.separator + remoteFileName);
                if (!localFile.exists() || localFile.length() != dFile.getRemoteSize()) {
                    downloadFile(dFile.getRemoteFile(), localFile);
                }
                if (existing.contains(remoteFileName)) {
                    existing.remove(remoteFileName);
                }
                result.add(localFile);
            }
            // Checking local files which do not exist remotely
            for (String toDelete : existing) {
                new File(localFolder + File.separator + toDelete).delete();
            }
        } else {
            LOGGER.error("Nothing to synchronize in " + remoteFolder);
        }
        return result;
    }

    public static List<String> listRemoteFilesInDir(String remoteDirectory) {
        return listRemotesInDir(remoteDirectory, true);
    }

    public static List<String> listRemoteFoldersInDir(String remoteDirectory) {
        return listRemotesInDir(remoteDirectory, false);
    }

    private static List<String> listRemotesInDir(String remoteDirectory, boolean filesOnly) {
        initIfNeeded();
        return Lists.newArrayList(Collections2.transform(listRemoteDFilesInDir(remoteDirectory, filesOnly), new Function<DropBoxFile, String>() {
            //@Override
            public String apply(DropBoxFile from) {
                return from.getRemoteFile();
            }
        }));
    }

    public static List<DropBoxFile> listRemoteDFilesInDir(String remoteDirectory, boolean filesOnly) {
        initIfNeeded();
        List<DropBoxFile> files = Lists.newArrayList();
        try {
            for (DbxEntry child : client.getMetadataWithChildren(remoteDirectory).children) {
                if (filesOnly && child.isFile() || !filesOnly && child.isFolder()) {
                    files.add(new DropBoxFile(remoteDirectory + SEPARATOR + child.name, child.isFile() ? child.asFile().numBytes : 0));
                }
            }
        } catch (DbxException e) {
            LOGGER.error("Could not list files for remote folder " + remoteDirectory + ": " + e.getMessage());
        }
        return files;
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

    //@Deprecated
    private static final class DropBoxFile {

        private File localFile;
        private String remoteFile;
        private long remoteSize;

        public DropBoxFile(File localFile, String remoteFile) {
            this.localFile = localFile;
            this.remoteFile = remoteFile;
        }

        public DropBoxFile(String remoteFile, long remoteSize) {
            this.remoteFile = remoteFile;
            this.remoteSize = remoteSize;
        }

        public File getLocalFile() {
            return localFile;
        }

        public String getRemoteFile() {
            return remoteFile;
        }

        public long getRemoteSize() {
            return this.remoteSize;
        }

        @Override
        public String toString() {
            return getRemoteFile() + " " + getLocalFile().getAbsolutePath();
        }

    }

}
