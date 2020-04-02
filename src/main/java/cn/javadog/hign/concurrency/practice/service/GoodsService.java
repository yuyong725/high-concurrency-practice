package cn.javadog.hign.concurrency.practice.service;

import cn.javadog.hign.concurrency.practice.dto.GoodsDto;

/**
 * @author 余勇
 * @date 2020年03月29日 20:31:00
 * 商品服务
 *
 * 注意点：
 * 1、对比 JDK 动态代理 与CGlib 动态代理的性能差别
 */
public interface GoodsService {


	/**
	 * 生成商品以及商品关联表的数据
	 */
	boolean generateData();

	/**
	 * 购买商品
	 * 直接从mysql中获取
	 */
	GoodsDto buy01(Integer goodsId, Integer buyNum);

	/**
	 * 在buy01的基础上，增加事务
	 */
	GoodsDto buy02(Integer goodsId, Integer buyNum);

	/**
	 * 悲观锁
	 */
	GoodsDto buy03(Integer goodsId, Integer buyNum);

	/**
	 * 乐观锁
	 */
	GoodsDto buy04(Integer goodsId, Integer buyNum);

	/**
	 * 分布式锁 + 异步
	 */
	void buy05(Integer goodsId, Integer buyNum);

	/**
	 * redis分布式锁+mysql
	 */
	GoodsDto buy06(Integer goodsId, Integer buyNum);
}
