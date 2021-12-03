package com.jorferri.sftpUploadReactive.repository;

import reactor.core.publisher.Flux;

import java.util.Map;

public interface ExtractRepository {

    Flux<Map<String, Object>> runExtract();
}
