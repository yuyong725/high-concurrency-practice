package cn.javadog.hign.concurrency.practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author 余勇
 * @date 2020年03月29日 13:59:00
 */
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy=true)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


}
