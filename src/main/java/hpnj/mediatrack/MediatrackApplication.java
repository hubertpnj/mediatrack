package hpnj.mediatrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MediatrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediatrackApplication.class, args);
	}

}
