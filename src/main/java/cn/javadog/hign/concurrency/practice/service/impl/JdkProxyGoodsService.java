package cn.javadog.hign.concurrency.practice.service.impl;

import cn.javadog.hign.concurrency.practice.config.AsyncConfig;
import cn.javadog.hign.concurrency.practice.constants.CategoryConstans;
import cn.javadog.hign.concurrency.practice.dto.GoodsDto;
import cn.javadog.hign.concurrency.practice.exception.DbKeyException;
import cn.javadog.hign.concurrency.practice.exception.LatchException;
import cn.javadog.hign.concurrency.practice.service.GoodsService;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import static cn.javadog.hign.concurrency.practice.constants.SqlContants.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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

	private static final Random RANDOM = new Random();
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
	@Async(AsyncConfig.EXECUTOR_ONE_BEAN_NAME)
	@Transactional(rollbackFor = Exception.class)
	public void generateOneData(Date date) {
		// 随机一个类目id
		int categoryId = RANDOM.nextInt(10) + 1;
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


	/**
	 * @date 2020-04-01 13:23
	 * 平均响应时间(去掉启动后的第一次)：（16+15+10） / 3 =14ms
	 * 库存剩余16件，销量79件，且数据库销量有多条相同记录。随着次数的增加，库存越来越接近0，超卖越来越少。
	 *
	 * 结论：
	 * 1、超卖
	 * 2、由于销量的创建是懒存储，当有第一笔订单时才添加销量。因此出现了多条记录，原理类似某些错误单例模式在并发情况下出现的多个实例
	 *
	 * 题外话：一开始与buy02同时运行出现如下问题，也说明项目中不能有其他类似操作数据库的方法，否则容易出现异常
	 * 1、运行报错，goods_stock 数据超纲，理论上说只有减到负数才会如此，实际数据库并没有
	 * 2、goods_sale 产生多条记录，原因很简单，类似于单例模式由于并发问题导致多个实例一样
	 */
	@Override
	public GoodsDto buy01(Integer goodsId, Integer buyNum) {
		// 读取库存数量
		Integer stock = jdbcTemplate.queryForObject("SELECT num FROM goods_stock WHERE goods_id = ?",
			Integer.class, goodsId) - 1;
		// 数量OK更新库存
		jdbcTemplate.update("UPDATE goods_stock SET num = ?, update_time = NOW() WHERE goods_id = ?",
			stock, goodsId);
		// 查询销量
		List<Map<String, Object>> goodsSales = jdbcTemplate.queryForList("SELECT id, num FROM goods_sale WHERE " +
			"goods_id = ?", goodsId);
		// 更新销量
		int sale;
		if (goodsSales.isEmpty()) {
			sale = 1;
			jdbcTemplate.update("INSERT INTO goods_sale(goods_id, num, create_time, update_time) VALUES(?, 1, NOW(), " +
					"NOW())", goodsId);
		}else {
			sale = (Integer) goodsSales.get(0).get("num") + 1;
			jdbcTemplate.update("UPDATE goods_sale SET num = ?, update_time = NOW() WHERE goods_id = ?",
				 sale, goodsId);
		}
		// 查询商品详情
		String querySQL =
			"SELECT " +
				"g.id id,g.name name, c.name category " +
			"FROM " +
				"goods g, goods_category gc, category c " +
			"WHERE " +
				"g.id = ? AND gc.goods_id = g.id  AND  gc.category_id=c.id;";

		Map<String, Object> objectMap = jdbcTemplate.queryForMap(querySQL, goodsId);
		GoodsDto dto = GoodsDto.transfer(objectMap);
		dto.setSale(sale);
		dto.setStock(stock);

		return dto;
	}

	/**
	 * @date 2020-04-01 14:03
	 * 平均响应时间(去掉启动后的第一次)：(32+48+29)/3=36ms
	 * 库存剩余53件，销量47件，且数据库销量有多条相同记录。库存销量之和始终为100
	 *
	 * 结论：
	 * 1、超卖
	 * 2、事务保证了更新库存和更新销量的一致性，因此保证了两者之和为100不变
	 * 3、事务增加了消耗，相对而言响应较慢，超卖更多
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public GoodsDto buy02(Integer goodsId, Integer buyNum) {
		return buy01(goodsId, buyNum);
	}

	/**
	 * @date 2020-04-01 17:48
	 *
	 * 平均响应时间(去掉启动后的第一次)：（20+12+11） / 3 =14ms
	 * 库存剩余0件，销量100件，始终如此，没有超卖
	 *
	 * 结论：
	 * 1、成功秒杀
	 * 2、响应很快，并没有因为锁响应速度收到影响
	 *
	 * 题外话：一开始没有加事务，发现和buy01效果一样，这是因为悲观锁，或者说排他锁的释放时机是事务结束，没有事务锁不生效
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public GoodsDto buy03(Integer goodsId, Integer buyNum) {
		// 读取库存数量
		int stock = jdbcTemplate.queryForObject("SELECT num FROM goods_stock WHERE goods_id = ? FOR UPDATE",
			Integer.class, goodsId) - 1;
		// 数量OK更新库存
		jdbcTemplate.update("UPDATE goods_stock SET num = ?, update_time = NOW() WHERE goods_id = ?"
			,stock, goodsId);
		// 查询销量
		List<Map<String, Object>> goodsSales = jdbcTemplate.queryForList("SELECT id, num FROM goods_sale WHERE " +
			"goods_id = ? FOR UPDATE", goodsId);
		// 更新销量
		int sale;
		if (goodsSales.isEmpty()) {
			sale = 1;
			jdbcTemplate.update("INSERT INTO goods_sale(goods_id, num, create_time, update_time) VALUES(?, 1, NOW(), " +
					"NOW())",
				goodsId);
		}else {
			sale = (Integer) goodsSales.get(0).get("num") + 1;
			jdbcTemplate.update("UPDATE goods_sale SET num = ?, update_time = NOW() WHERE goods_id = ?",
				sale, goodsId);
		}
		// 查询商品详情
		String querySQL =
			"SELECT " +
				"g.id id,g.name name, c.name category " +
			"FROM " +
				"goods g, goods_category gc, category c " +
			"WHERE " +
				"g.id = ? AND gc.goods_id = g.id  AND  gc.category_id=c.id;";

		Map<String, Object> objectMap = jdbcTemplate.queryForMap(querySQL, goodsId);
		GoodsDto dto = GoodsDto.transfer(objectMap);
		dto.setSale(sale);
		dto.setStock(stock);

		return dto;
	}

	/**
	 * @date 2020-04-01 19:29
	 *
	 * 平均响应时间(去掉启动后的第一次)：（20+12+11） / 3 =14ms
	 * 库存剩余0件，销量100件，始终如此，没有超卖
	 *
	 * 结论：
	 * 1、成功秒杀
	 * 2、响应很快，并没有因为锁响应速度收到影响
	 * 3、虽然最终的库存和销量是正确的，但过程中两者之和不为100，因为两步是在不同的乐观锁逻辑之中，这对业务逻辑影响较小，
	 * 	但依然有一定影响！
	 *
	 */
	@Override
	public GoodsDto buy04(Integer goodsId, Integer buyNum) {
		int updateStockCnt = 0;
		int stock = 0;
		while (updateStockCnt == 0) {
			// 读取库存数量
			stock = jdbcTemplate.queryForObject("SELECT num FROM goods_stock WHERE goods_id = ?", Integer.class,
				goodsId) - 1;
			// 数量OK更新库存
			updateStockCnt = jdbcTemplate.update("UPDATE goods_stock SET num = ?, update_time = NOW() WHERE goods_id " +
				"= ? AND num = ?", stock, goodsId, stock + 1);
		}

		int updateSaleCnt = 0;
		int sale = 0;
		while (updateSaleCnt == 0) {
			// 查询销量
			List<Map<String, Object>> goodsSales = jdbcTemplate.queryForList("SELECT id, num FROM goods_sale WHERE " +
				"goods_id = ?", goodsId);
			// 更新销量
			if (goodsSales.isEmpty()) {
				sale = 1;
				updateSaleCnt = jdbcTemplate.update(
					"INSERT INTO " +
							"goods_sale(goods_id, num, create_time, update_time) " +
						"SELECT ?,1,NOW(),NOW() from dual WHERE NOT EXISTS  (" +
							"SELECT num FROM goods_sale WHERE goods_id = ?)",
					goodsId, goodsId);
			}else {
				sale = (Integer) goodsSales.get(0).get("num") + 1;
				updateSaleCnt = jdbcTemplate.update("UPDATE goods_sale SET num = ?, update_time = NOW()" +
					" WHERE goods_id = ? AND num = ?", sale, goodsId, sale-1);
			}
		}

		// 查询商品详情
		String querySQL =
			"SELECT " +
				"g.id id,g.name name, c.name category " +
				"FROM " +
				"goods g, goods_category gc, category c " +
				"WHERE " +
				"g.id = ? AND gc.goods_id = g.id  AND  gc.category_id=c.id;";

		Map<String, Object> objectMap = jdbcTemplate.queryForMap(querySQL, goodsId);
		GoodsDto dto = GoodsDto.transfer(objectMap);
		dto.setSale(sale);
		dto.setStock(stock);

		return dto;
	}

	@Autowired
	private RedissonClient redissonClient;

	private static final String LOCK_KEY = "buy05lock";

	/**
	 * @date 2020-04-02 12:54
	 *
	 * 平均响应时间(去掉启动后的第一次)：（23+10+12） / 3 =15ms
	 * 库存剩余0件，销量100件，始终如此，没有超卖
	 *
	 * 结论：
	 * 1、成功秒杀
	 * 2、响应很快，并没有因为锁响应速度收到影响
	 * 3、将mysql数据库事务可能的问题转给redis，然后将任务存储到队列串行执行
	 */
	@Override
	public void buy05(Integer goodsId, Integer buyNum) {
		final RLock lock = redissonClient.getLock(LOCK_KEY);
		lock.lock();
		ListenableFuture<GoodsDto> future = self().asyncBuy05(goodsId, buyNum);
		lock.unlock();
		// 这里也可以等待 asyncBuy05 返回，但意义不大，保证不超卖就好
		// future.get();
	}

	@Async(AsyncConfig.EXECUTOR_TWO_BEAN_NAME)
	public ListenableFuture<GoodsDto> asyncBuy05(Integer goodsId, Integer buyNum) {
		GoodsDto goodsDto = buy01(goodsId, buyNum);
		// 校验下线程池是否生效
		// logger.info("当前线程名：" + Thread.currentThread().getName());
		return AsyncResult.forValue(goodsDto);
	}



	@Override
	public GoodsDto buy06(Integer goodsId, Integer buyNum) {
		return null;
	}


}
