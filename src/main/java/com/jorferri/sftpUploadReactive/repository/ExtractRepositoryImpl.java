package com.jorferri.sftpUploadReactive.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Map;

@Repository
public class ExtractRepositoryImpl implements ExtractRepository{

    @Autowired
    R2dbcEntityTemplate template;


    @Override
    public Flux<Map<String, Object>> runExtract() {
        return template
                .getDatabaseClient()
                .sql("SELECT * FROM CONTACTS c ")
//                .bind("name", name)
//                .map((row, rowMetadata) -> (String)row)
                .fetch()
                .all();
    }
}
