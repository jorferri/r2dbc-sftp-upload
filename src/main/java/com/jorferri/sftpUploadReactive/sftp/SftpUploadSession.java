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

import java.io.OutputStream;
import java.util.Hashtable;

@RequiredArgsConstructor
@Slf4j
public class SftpUploadSession {

    @NonNull
    @Getter
    String file;

    @NonNull
    String host;

    int port;

    @NonNull
    String username;

    @NonNull
    String password;

    ChannelSftp sftpChannel;
    Session session;
    OutputStream outputStream;

    String lineSeparator = System.getProperty("line.separator");

    @SneakyThrows
    public SftpUploadSession init() {
        JSch jsch = new JSch();
        Hashtable<String, String> config = new Hashtable<>();
        config.put("StrictHostKeyChecking", "no");
        JSch.setConfig(config);
        session = jsch.getSession( username, host, port);
        session.setPassword(password);
        session.connect();
        Channel channel = session.openChannel( "sftp" );
        channel.connect();
        sftpChannel = (ChannelSftp) channel;
        outputStream = sftpChannel.put(file, ChannelSftp.OVERWRITE);
        return this;
    }

    @SneakyThrows
    public SftpUploadSession write(String s) {
        outputStream.write(s.getBytes());
        outputStream.write(lineSeparator.getBytes());
        log.info("Write " + file + " via " + Thread.currentThread().getName());
        return this;
    }

    @SneakyThrows
    public void close() {
        outputStream.close();
        if (sftpChannel.isConnected())
            sftpChannel.exit();
        if (session.isConnected())
            session.disconnect();
        log.info("Closed " + file + " via " + Thread.currentThread().getName());
    }
}
