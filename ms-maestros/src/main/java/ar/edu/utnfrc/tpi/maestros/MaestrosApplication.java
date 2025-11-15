package ar.edu.utnfrc.tpi.maestros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class MaestrosApplication {
    public static void main(String[] args) {
        // 🔒 Fuerza la TZ del JVM ANTES de abrir conexiones JDBC
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
        SpringApplication.run(MaestrosApplication.class, args);
    }
}

