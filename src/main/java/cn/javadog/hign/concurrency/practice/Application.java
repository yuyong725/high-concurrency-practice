package cn.javadog.hign.concurrency.practice;

import java.util.List;

import cn.javadog.hign.concurrency.practice.constants.CategoryConstans;
import cn.javadog.hign.concurrency.practice.entity.Category;
import cn.javadog.hign.concurrency.practice.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author 余勇
 * @date 2020年03月29日 13:59:00
 */
@EnableAsync
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy=true)
public class Application implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void run(String... args) throws Exception {
		// 写入类目数据
		String querySQL = "SELECT id,name FROM category";
		List<Category> categories = jdbcTemplate.query(querySQL, new BeanPropertyRowMapper<>(Category.class));
		categories.forEach(c -> CategoryConstans.categoryInfos.put(c.getId(), c.getName()));
		// 跑基础数据
		// goodsService.generateData();
	}
}