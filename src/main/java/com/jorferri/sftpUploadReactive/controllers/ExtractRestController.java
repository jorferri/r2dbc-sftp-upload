package com.jorferri.sftpUploadReactive.controllers;

import com.jorferri.sftpUploadReactive.services.ExtractService;
import com.jorferri.sftpUploadReactive.sftp.SftpUploadSession;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@Api(value = "Extract Controller")
public class ExtractRestController {

    @Autowired
    ExtractService extractService;

    @GetMapping("/extract/{name}/{day}")
    @ApiOperation(value = "Generate extract")
    @ResponseBody
    public ResponseEntity<String> getExtract (
            @ApiParam(value = "name", example = "extractName")
            @PathVariable(value = "name") String name,
            @ApiParam(value = "day", example = "2021-12-01")
            @PathVariable(value = "day")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate day
            ) {

        Flux<Map<String, Object>> flux = extractService.runExtract();

        Flux<String> stringFlux = flux
                .map(Object::toString)
                .startWith("HEADER")
                .doOnNext(s -> log.info("Row->" + s))
//                .subscribeOn(Schedulers.newParallel("file-copy", 3))
//                .publishOn(Schedulers.newParallel("file-copy", numTargets))
//                .log()
                .share();

        // output file
        Flux.just(
                        new SftpUploadSession(
                                "/upload/large-output-file1.txt",
                                "localhost",
                                2221,
                                "foo1",
                                "pass1"
                        ),
                        new SftpUploadSession(
                                "/upload/large-output-file2.txt",
                                "localhost",
                                2222,
                                "foo2",
                                "pass2"
                        ),
                        new SftpUploadSession(
                                "/upload/large-output-file3.txt",
                                "localhost",
                                2223,
                                "foo3",
                                "pass3"
                        )
                )
                .map(
                        sftpUploadSession ->
                                Mono.just(sftpUploadSession)
                                        .map(SftpUploadSession::init)
                                        .map(upload -> stringFlux
                                                .map(upload::write)
                                                .subscribe(s -> log.info("File " + upload.getFile() + " uploaded successfully"),
                                                        (e) -> upload.close(),  // close file if error / oncomplete
                                                        upload::close))
                                        .retry(3)
                                        .doOnError(throwable -> log.error("File couldn't be uploaded:" + sftpUploadSession.getFile(), throwable))
                                        .subscribeOn(Schedulers.boundedElastic()) //to avoid blocking
                                        .subscribe()

                )
//                .log()
                .subscribe()
        ;

        return new ResponseEntity<>("extract triggered", HttpStatus.OK);
    }
}
