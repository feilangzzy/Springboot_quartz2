package com.feilangzzy.quartz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.feilangzzy.quartz.mapper")
public class IvrApplication {
	public static void main(String[] args) {
		SpringApplication.run(IvrApplication.class, args);
	}
}
