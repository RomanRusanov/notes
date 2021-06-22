package ru.rrusanov.notes;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.Date;

@SpringBootApplication
public class NotesApplication {
    /**
     * The bean instance for Liquibase config.
     * @param ds Data source config DB connection.
     * @return Configured liquibase instance.
     */
    @Bean
    public SpringLiquibase liquibase(DataSource ds) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:liquibase-changeLog.xml");
        liquibase.setDataSource(ds);
        return liquibase;
    }

    /**
     * Entry point.
     * @param args Passed args.
     */
    public static void main(String[] args) {
        SpringApplication.run(NotesApplication.class, args);
    }

}
