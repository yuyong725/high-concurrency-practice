package cn.javadog.hign.concurrency.practice.service.impl;

import cn.javadog.hign.concurrency.practice.constants.CategoryConstans;
import cn.javadog.hign.concurrency.practice.exception.DbKeyException;
import cn.javadog.hign.concurrency.practice.exception.LatchException;
import cn.javadog.hign.concurrency.practice.service.GoodsService;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static cn.javadog.hign.concurrency.practice.constants.SqlContants.*;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author 余勇
 * @date 2020年03月29日 20:32:00
 */
@Service
public class JdkProxyGoodsService implements GoodsService {

	private Logger logger = LoggerFactory.getLogger(JdkProxyGoodsService.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static final Random categoryRandom = new Random();
	private static CountDownLatch latch = new CountDownLatch(1000);

	/**
	 * 拿到代理对象
	 */
	private JdkProxyGoodsService self() {
		return (JdkProxyGoodsService) AopContext.currentProxy();
	}

	@Override
	public boolean generateData() {
		// 200万条数据, 一次10000条
		for (int i = 0; i < 200; i++) {
			Date date = new Date();
			for (int j = 0; j < 10000; j++) {
				self().generateOneData(date);
			}
			try {
				latch.await();
			} catch (InterruptedException e) {
				throw new LatchException();
			}
			logger.info("以成功插入[{}万]条商品数据", i+1);
			latch = new CountDownLatch(10000);
		}
		return true;
	}

	/**
	 * 1、新增商品，返回主键
	 * 2、根据返回的主键，新增商品分类，商品库存
	 * 	2.1、商品分类的类目ID使用随机值，商品销量懒存储，也就是卖了第一件时才存储
	 *
	 * note: @Transactional的作用：https://blog.csdn.net/weixin_40910372/article/details/103565970?depth_1-utm_source=distribute.pc_relevant.none-task&utm_source=distribute.pc_relevant.none-task
	 */
	@Async
	@Transactional(rollbackFor = Exception.class)
	public void generateOneData(Date date) {
		// 随机一个类目id
		int categoryId = categoryRandom.nextInt(10) + 1;
		// 插入商品
		int goodsId;
		// 创建 KeyHolder 对象，设置返回的主键 ID
		KeyHolder keyHolder = new GeneratedKeyHolder();
		// 拿到指定类目名，用于生成商品名
		String goodsName = CategoryConstans.getCategoryNameById(categoryId) + RandomStringUtils.randomAlphabetic(5);
		// 生成随机的商品描述
		String goodsDetail = RandomStringUtils.randomAlphabetic(1000);
		// 执行插入商品操作
		jdbcTemplate.update(INSERT_GOODS_PREPARED_STATEMENT_CREATOR_FACTORY.newPreparedStatementCreator(
			Arrays.asList(goodsName, goodsDetail)
		), keyHolder);
		// 设置 ID 主键到 entity 实体中
		if (keyHolder.getKey() != null) {
			goodsId = keyHolder.getKey().intValue();
		} else {
			throw new DbKeyException();
		}

		// 插入分类
		jdbcTemplate.update(INSERT_GOODS_CATEGORY_PREPARED_STATEMENT_CREATOR_FACTORY
			.newPreparedStatementCreator(Arrays.asList(goodsId, categoryId)
		));
		// 插入库存
		jdbcTemplate.update(INSERT_GOODS_STOCK_PREPARED_STATEMENT_CREATOR_FACTORY
			.newPreparedStatementCreator(Arrays.asList(goodsId, 100, date, date)
		));

		// 计数
		latch.countDown();
	}


}
