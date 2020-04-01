package cn.javadog.hign.concurrency.practice.dto;

import java.util.Map;

import lombok.Data;

/**
 * @author 余勇
 * @date 2020年03月31日 16:45:00
 */
@Data
public class GoodsDto {

	/**
	 * 商品ID
	 */
	private Integer id;

	/**
	 * 商品名
	 */
	private String name;

	/**
	 * 商品所属分类
	 */
	private String category;

	/**
	 * 商品详情
	 */
	private String detail;

	/**
	 * 商品销量
	 */
	private Integer sale;

	/**
	 * 剩余库存
	 */
	private Integer stock;

	public static GoodsDto transfer(Map<String, Object> dataMap) {
		GoodsDto dto = new GoodsDto();
		dto.setId(Integer.parseInt(dataMap.get("id").toString()));
		dto.setName(dataMap.get("name").toString());
		dto.setCategory(dataMap.get("category").toString());
		return dto;
	}
}
