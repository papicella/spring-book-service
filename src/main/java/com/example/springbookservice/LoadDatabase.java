package com.example.springbookservice;

import com.example.springbookservice.model.Book;
import com.example.springbookservice.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(BookRepository repository) {
        return args -> {
            log.info("Preloading " + repository.save(new Book("Flexible Rails - Pas", "Peter Armstrong")));
            log.info("Preloading " + repository.save(new Book("Brownfield Application Development in .NET", "Kyle Baley")));
            log.info("Preloading " + repository.save(new Book("MongoDB in Action", "Kyle Banker")));
            log.info("Preloading " + repository.save(new Book("Java Persistence with Hibernate", "Christian Bauer")));
            log.info("Preloading " + repository.save(new Book("POJO's In Action", "Chris Richardson")));
        };
    }
}
