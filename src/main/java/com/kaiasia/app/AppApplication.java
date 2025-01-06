package com.kaiasia.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

import java.util.Random;

@SpringBootApplication
@ImportResource({"classpath:spring-beans-dao.xml"})
@ComponentScan(basePackages = {"com.kaiasia", "ms.apiclient"})
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }


    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> {
            A a = new A();

            B b = new B();
            b.a = a;
            new C().change(b);
            System.out.println(a.a);
        };
    }
}


class A {
    public int a = 0;
}

class B {
    A a = new A();
}

class C {
    public void change(B b) {
        b.a.a = new Random().nextInt();
    }
}