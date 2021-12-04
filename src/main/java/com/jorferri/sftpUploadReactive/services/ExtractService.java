package com.jorferri.sftpUploadReactive.services;

import com.jorferri.sftpUploadReactive.repository.ExtractRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class ExtractService {

    @Autowired
    ExtractRepository repository;

    public Flux<String> runExtract() {
        return repository.runExtract().map(Object::toString)
                .startWith("HEADER")
//                .doOnNext(s -> log.info("Row->" + s))
//                .subscribeOn(Schedulers.newParallel("file-copy", 3))
//                .publishOn(Schedulers.newParallel("file-copy", numTargets))
//                .log()
                .share();
    }
}
