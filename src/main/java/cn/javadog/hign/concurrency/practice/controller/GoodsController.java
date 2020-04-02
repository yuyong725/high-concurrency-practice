package cn.javadog.hign.concurrency.practice.controller;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import cn.javadog.hign.concurrency.practice.dto.GoodsDto;
import cn.javadog.hign.concurrency.practice.service.GoodsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 余勇
 * @date 2020年03月29日 14:00:00
 * 商品接口
 */
@RestController
@RequestMapping("goods")
public class GoodsController {

	private Logger logger = LoggerFactory.getLogger(GoodsController.class);

	/**
	 * 计数器，带状态的，因为Spring是单例
	 */
	private AtomicInteger counter = new AtomicInteger(0);

	@Autowired
	private GoodsService goodsService;


	/**
	 * 测试吞吐
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
	@GetMapping("buy01")
	public String buy01(@RequestParam Integer goodsId,  @RequestParam(defaultValue = "1") Integer buyNum) {
		GoodsDto goodsDto = goodsService.buy01(goodsId, buyNum);
		String format = String.format("成功购买 %s 件 %s，当前销量 %s 件，剩余库存 %s 件",
			buyNum, goodsDto.getName(), goodsDto.getSale(), goodsDto.getStock());
		logger.info(format);
		return format;
	}

	@GetMapping("buy02")
	public String buy02(@RequestParam Integer goodsId,  @RequestParam(defaultValue = "1") Integer buyNum) {
		GoodsDto goodsDto = goodsService.buy02(goodsId, buyNum);
		String format = String.format("成功购买 %s 件 %s，当前销量 %s 件，剩余库存 %s 件",
			buyNum, goodsDto.getName(), goodsDto.getSale(), goodsDto.getStock());
		logger.info(format);
		return format;
	}

	@GetMapping("buy03")
	public String buy03(@RequestParam Integer goodsId,  @RequestParam(defaultValue = "1") Integer buyNum) {
		GoodsDto goodsDto = goodsService.buy03(goodsId, buyNum);
		String format = String.format("成功购买 %s 件 %s，当前销量 %s 件，剩余库存 %s 件",
			buyNum, goodsDto.getName(), goodsDto.getSale(), goodsDto.getStock());
		logger.info(format);
		return format;
	}

	@GetMapping("buy04")
	public String buy04(@RequestParam Integer goodsId,  @RequestParam(defaultValue = "1") Integer buyNum) {
		GoodsDto goodsDto = goodsService.buy04(goodsId, buyNum);
		String format = String.format("成功购买 %s 件 %s，当前销量 %s 件，剩余库存 %s 件",
			buyNum, goodsDto.getName(), goodsDto.getSale(), goodsDto.getStock());
		logger.info(format);
		return format;
	}

	private AtomicInteger buy05Counter = new AtomicInteger(0);

	@GetMapping("buy05")
	public String buy05(@RequestParam Integer goodsId,  @RequestParam(defaultValue = "1") Integer buyNum) {
		goodsService.buy05(goodsId, buyNum);
		int sale = buy05Counter.incrementAndGet();
		String format = String.format("成功购买 %s 件商品，当前销量 %s 件", buyNum, sale);
		logger.info(format);
		return format;
	}

}
