package com.openshift.evg.roadshow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by jmorales on 24/08/16.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.openshift.evg.roadshow.rest")
public class ParksMapApplication {

  public static void main(String[] args) {
    SpringApplication.run(ParksMapApplication.class, args);
  }

}
