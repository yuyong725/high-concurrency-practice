package cn.javadog.hign.concurrency.practice.exception;

/**
 * @author 余勇
 * @date 2020年03月30日 15:02:00
 */
public class LatchException extends BaseException {

	public LatchException() {
		super("闭锁等待异常");
	}
}
