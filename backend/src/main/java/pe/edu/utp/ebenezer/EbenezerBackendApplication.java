package pe.edu.utp.ebenezer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EbenezerBackendApplication {

	public static void main(String[] args) {
		// HikariCP pings the database before lending a connection idle for more than 500 ms, which costs a full
		// network round trip per request with a remote database. Idle connections are already checked by
		// spring.datasource.hikari.keepalive-time, so skip that ping for connections used in the last 30 s.
		if (System.getProperty("com.zaxxer.hikari.aliveBypassWindowMs") == null) {
			System.setProperty("com.zaxxer.hikari.aliveBypassWindowMs", "30000");
		}
		SpringApplication.run(EbenezerBackendApplication.class, args);
	}

}
