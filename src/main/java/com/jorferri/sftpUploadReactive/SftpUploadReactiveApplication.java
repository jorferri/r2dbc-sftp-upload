package com.jorferri.sftpUploadReactive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@Slf4j
@Configuration
public class SftpUploadReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(SftpUploadReactiveApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void applicationReady(ApplicationReadyEvent event) {
		log.info("GO GO GO!!! FIRE IN THE HOLE!!!");
	}
}
