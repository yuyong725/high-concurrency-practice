package cn.javadog.hign.concurrency.practice.controller;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 余勇
 * @date 2020年03月29日 14:00:00
 * 商品接口
 */
@RestController
@RequestMapping("goods")
public class GoodsController {

	private AtomicInteger counter = new AtomicInteger(0);

	/**
	 * 测试
	 */
	@GetMapping("echo")
	public String echo() throws Exception {
		int value = counter.incrementAndGet();
		System.out.println(value);
		Thread.sleep(new Random().nextInt(1000));
		return "echo";
	}

	/**
	 * @author 余勇
	 * @date 2020-03-29 19:13
	 *
	 * 秒杀
	 *
	 * 思路：
	 * 1、MySQL数据库准备1000万商品，商品关联10张表，商品表含有一个大字段
	 * 2、通过最简单的 jdbctemplate 连接数据库，并准备redis
	 * 3、测试查询并发瓶颈，得到最终响应时间与硬件配置的关系
	 * 4、设计秒杀逻辑，如一万件商品参加秒杀活动，每个商品1000件，测试事务下的性能瓶颈在数据库还是服务器
	 */


}
