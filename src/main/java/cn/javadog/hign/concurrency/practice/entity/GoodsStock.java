package cn.javadog.hign.concurrency.practice.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * @author 余勇
 * @date 2020年03月30日 10:45:00
 */
@Data
public class GoodsStock {

	private Integer id;

	private Integer goodsId;

	private Integer num;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

}
