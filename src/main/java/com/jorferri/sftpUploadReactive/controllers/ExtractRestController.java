package com.jorferri.sftpUploadReactive.controllers;

import com.jorferri.sftpUploadReactive.services.ExtractService;
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
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;
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

        int numTargets = 3;

        Flux<String> stringFlux = flux
                .map(stringObjectMap -> stringObjectMap.toString())
                .doOnNext(s -> log.info("Row->" + s))
//                .subscribeOn(Schedulers.newParallel("file-copy", numTargets))
//                .publishOn(Schedulers.newParallel("file-copy", numTargets))
                .log()
                .share();

        // output file
        Flux.range(0, numTargets)
                .map(integer -> Paths.get("/home/jorge/Downloads/large-output-file" + integer + ".txt"))
                .map(path -> getBufferedWriter(path))
                .map(bufferedWriter -> stringFlux
                        .subscribe(s -> {
                            write(bufferedWriter, s);
                            log.info("Received via " + Thread.currentThread().getName());
                            },
                                (e) -> close(bufferedWriter),  // close file if error / oncomplete
                                () -> close(bufferedWriter))
                )
                .log()
                .subscribe()
        ;

        return new ResponseEntity<>("extract triggered", HttpStatus.OK);
    }

    private BufferedWriter getBufferedWriter(Path opPath) {
        BufferedWriter bw = null;
        try {
            bw = Files.newBufferedWriter(opPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bw;
    }

    // private methods to handle checked exceptions

    private void close(Closeable closeable){
        try {
            closeable.close();
            log.info("Closed the resource");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void write(BufferedWriter bw, String string){
        try {
            bw.write(string);
            bw.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
