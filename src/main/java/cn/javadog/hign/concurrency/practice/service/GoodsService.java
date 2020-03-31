package cn.javadog.hign.concurrency.practice.service;

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

}
