package com.jorferri.sftpUploadReactive.sftp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Hashtable;

@RequiredArgsConstructor
@Slf4j
public class SftpUploadSession {

    @NonNull
    @Getter
    String file;

    @NonNull
    String host;

    @NonNull
    Integer port;

    @NonNull
    String username;

    @NonNull
    String password;

    ChannelSftp sftpChannel;
    Session session;
    DigestOutputStream outputStream;

    String lineSeparator = System.getProperty("line.separator");

    @SneakyThrows
    public SftpUploadSession init() {
        JSch jsch = new JSch();
        Hashtable<String, String> config = new Hashtable<>();
        config.put("StrictHostKeyChecking", "no");
        JSch.setConfig(config);

//        String privateKey = ".ssh/id_rsa";
//        jsch.addIdentity(privateKey); //remove password

        session = jsch.getSession( username, host, port);
        session.setPassword(password);
        session.connect();
        Channel channel = session.openChannel( "sftp" );
        channel.connect();
        sftpChannel = (ChannelSftp) channel;
        MessageDigest md = MessageDigest.getInstance("MD5");
        outputStream = new DigestOutputStream(new BufferedOutputStream(sftpChannel.put(file, ChannelSftp.OVERWRITE)), md);
//        log.info("Created connection to sftp:" + username);
        return this;
    }

    @SneakyThrows
    public SftpUploadSession write(String s) {
        outputStream.write(s.getBytes());
        outputStream.write(lineSeparator.getBytes());
//        log.info("Write " + file + " via " + Thread.currentThread().getName());
        return this;
    }

    @SneakyThrows
    public void close() {
        outputStream.close();
        if (sftpChannel.isConnected())
            sftpChannel.exit();
        if (session.isConnected())
            session.disconnect();
        log.info("File " + file + " uploaded and connection closed via " + Thread.currentThread().getName());
    }

    // better to use external lib
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    private static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public void createMetadataAndClose() {
        byte[] digest = outputStream.getMessageDigest().digest();
        String md5 = bytesToHex(digest);
        log.info("Metadata for " + file + " uploaded:" + md5);
        outputStream.close();

        BufferedOutputStream metadata = new BufferedOutputStream(sftpChannel.put("/upload/metadata.txt", ChannelSftp.OVERWRITE));
        metadata.write(md5.getBytes());
        metadata.close();

        close();
    }
}
