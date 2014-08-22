package com.hp.picc.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration 
@ComponentScan("com.hp.picc") 
@EnableWebMvc  
public class AppConfig {

}
