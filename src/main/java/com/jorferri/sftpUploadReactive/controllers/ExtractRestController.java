package com.jorferri.sftpUploadReactive.controllers;

import com.jorferri.sftpUploadReactive.services.ExtractService;
import com.jorferri.sftpUploadReactive.services.SftpService;
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

import java.time.LocalDate;

@Slf4j
@RestController
@Api(value = "Extract Controller")
public class ExtractRestController {

    @Autowired
    ExtractService extractService;

    @Autowired
    SftpService sftpService;

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

        Flux<String> stringFlux = extractService.runExtract();

        // output file
        Flux<SftpUploadSession> targetSftps = Flux.just(
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
        );

        sftpService.uploadExtract(targetSftps, stringFlux);

        return new ResponseEntity<>("extract triggered", HttpStatus.OK);
    }
}
