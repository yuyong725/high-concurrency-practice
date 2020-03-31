package cn.javadog.hign.concurrency.practice.exception;

/**
 * @author 余勇
 * @date 2020年03月30日 14:40:00
 */
public class DbKeyException extends BaseException {


	public DbKeyException() {
		super("自增主键生成失败！");
	}
}
