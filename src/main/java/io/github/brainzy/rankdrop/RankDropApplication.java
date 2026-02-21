package io.github.brainzy.rankdrop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class RankDropApplication {

	public static void main(String[] args) {
		SpringApplication.run(RankDropApplication.class, args);
	}
}
