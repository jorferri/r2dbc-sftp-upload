package com.jorferri.sftpUploadReactive.services;

import com.jorferri.sftpUploadReactive.sftp.SftpUploadSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
public class SftpService {

    public Flux<SftpUploadSession> uploadExtract(Flux<SftpUploadSession> targetSftps, Flux<String> stringFlux) {
        return
                targetSftps.flatMap(
                        sftpUploadSession ->
                                Mono.just(sftpUploadSession)
                                        .map(SftpUploadSession::init)
                                        .flatMapMany(upload -> stringFlux
                                                .map(upload::write)
                                                .doOnComplete(upload::createMetadataAndClose)
                                                .doOnError((e) -> upload.close())
                                                        .reduce((sftpUploadSession1, sftpUploadSession2) -> sftpUploadSession)
//                                                .subscribe(s -> {},
//                                                        (e) -> upload.close(),  // close file if error / oncomplete
//                                                        upload::createMetadataAndClose)
                                        )
                                        .retryWhen(Retry.backoff(3, Duration.ofSeconds(10)).jitter(0.75))
                                        .doOnError(throwable -> log.error("File couldn't be uploaded:" + sftpUploadSession.getFile(), throwable))
                                        .subscribeOn(Schedulers.boundedElastic()) //to avoid blocking
//                                        .subscribe()

                )
//                .log()
//                .subscribe()
                ;
    }
}
