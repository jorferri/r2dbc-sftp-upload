package com.jorferri.sftpUploadReactive;

import com.jorferri.sftpUploadReactive.services.ExtractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableSwagger2
@Slf4j
@Configuration
public class SftpUploadReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(SftpUploadReactiveApplication.class, args);
	}


//	@Autowired
//	ExtractService extractService;


	@EventListener(ApplicationReadyEvent.class)
	public void applicationReady(ApplicationReadyEvent event) {
		log.info("GO GO GO!!! FIRE IN THE HOLE!!!");

//		Flux<Map<String, Object>> flux = extractService.runExtract();
//		List<Map<String, Object>> block = flux.collectList().block();
//
//		log.info("hola" + block.size());

	}
}
