package cn.javadog.hign.concurrency.practice.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 余勇
 * @date 2020年03月30日 14:21:00
 * 存储类目的信息，数据存在数据库里面，启动的时候拿出来
 */
public class CategoryConstans {

	public static final Map<Integer, String> categoryInfos = new HashMap<>(10);

	public static String getCategoryNameById(Integer categoryId) {
		return categoryInfos.get(categoryId);
	}

}
