package cn.javadog.hign.concurrency.practice.job;

import java.util.List;

import cn.javadog.hign.concurrency.practice.constants.CategoryConstans;
import cn.javadog.hign.concurrency.practice.entity.Category;
import cn.javadog.hign.concurrency.practice.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author 余勇
 * @date 2020年03月31日 16:04:00
 */
//@Component
public class StartupJob implements CommandLineRunner {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private GoodsService goodsService;

	@Override
	public void run(String... args) throws Exception {
		// 写入类目数据
		String querySQL = "SELECT id,name FROM category";
		List<Category> categories = jdbcTemplate.query(querySQL, new BeanPropertyRowMapper<>(Category.class));
		categories.forEach(c -> CategoryConstans.categoryInfos.put(c.getId(), c.getName()));
		// 跑基础数据
	 	goodsService.generateData();
	}

}
