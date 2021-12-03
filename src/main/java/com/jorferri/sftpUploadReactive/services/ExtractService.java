package com.jorferri.sftpUploadReactive.services;

import com.jorferri.sftpUploadReactive.repository.ExtractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
public class ExtractService {

    @Autowired
    ExtractRepository repository;

    public Flux<Map<String, Object>> runExtract(){
        return repository.runExtract();
    }
}
