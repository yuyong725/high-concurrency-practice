package cn.javadog.hign.concurrency.practice.constants;

import java.sql.Types;

import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.SqlParameter;

/**
 * @author 余勇
 * @date 2020年03月30日 13:08:00
 *
 * sql 常量
 */
public class SqlContants {

	/**
	 * 更新商品sql
	 */
	public static final PreparedStatementCreatorFactory INSERT_GOODS_PREPARED_STATEMENT_CREATOR_FACTORY
		= new PreparedStatementCreatorFactory("INSERT INTO goods(name, detail) " +
		"VALUES(?, ?)");

	/**
	 * 新增商品分类sql
	 */
	public static final PreparedStatementCreatorFactory INSERT_GOODS_CATEGORY_PREPARED_STATEMENT_CREATOR_FACTORY
		= new PreparedStatementCreatorFactory("INSERT INTO goods_category(goods_id, category_id) " +
		"VALUES(?, ?)");

	/**
	 * 新增商品销量sql
	 */
	public static final PreparedStatementCreatorFactory INSERT_GOODS_SALE_PREPARED_STATEMENT_CREATOR_FACTORY
		= new PreparedStatementCreatorFactory("INSERT INTO goods_sale(goods_id, num, create_time, update_time) " +
		"VALUES(?, ?, ?, ?)");

	/**
	 * 新增商品库存sql
	 */
	public static final PreparedStatementCreatorFactory INSERT_GOODS_STOCK_PREPARED_STATEMENT_CREATOR_FACTORY
		= new PreparedStatementCreatorFactory("INSERT INTO goods_stock(goods_id, num, create_time, update_time) " +
		"VALUES(?, ?, ?, ?)");

	static {
		/*——————————————————————————————————新增商品————————————————————————————————————————*/
		// 设置返回主键
		INSERT_GOODS_PREPARED_STATEMENT_CREATOR_FACTORY.setReturnGeneratedKeys(true);
		INSERT_GOODS_PREPARED_STATEMENT_CREATOR_FACTORY.setGeneratedKeysColumnNames("id");
		// 设置每个占位符的类型
		INSERT_GOODS_PREPARED_STATEMENT_CREATOR_FACTORY.addParameter(new SqlParameter(Types.VARCHAR));
		INSERT_GOODS_PREPARED_STATEMENT_CREATOR_FACTORY.addParameter(new SqlParameter(Types.VARCHAR));

		/*——————————————————————————————————新增分类————————————————————————————————————————*/
		// 设置每个占位符的类型
		INSERT_GOODS_CATEGORY_PREPARED_STATEMENT_CREATOR_FACTORY.addParameter(new SqlParameter(Types.INTEGER));
		INSERT_GOODS_CATEGORY_PREPARED_STATEMENT_CREATOR_FACTORY.addParameter(new SqlParameter(Types.INTEGER));

		/*——————————————————————————————————新增销量————————————————————————————————————————*/
		// 设置每个占位符的类型
		INSERT_GOODS_SALE_PREPARED_STATEMENT_CREATOR_FACTORY.addParameter(new SqlParameter(Types.INTEGER));
		INSERT_GOODS_SALE_PREPARED_STATEMENT_CREATOR_FACTORY.addParameter(new SqlParameter(Types.INTEGER));
		INSERT_GOODS_SALE_PREPARED_STATEMENT_CREATOR_FACTORY.addParameter(new SqlParameter(Types.TIMESTAMP));
		INSERT_GOODS_SALE_PREPARED_STATEMENT_CREATOR_FACTORY.addParameter(new SqlParameter(Types.TIMESTAMP));
		/*——————————————————————————————————新增库存————————————————————————————————————————*/
		// 设置每个占位符的类型
		INSERT_GOODS_STOCK_PREPARED_STATEMENT_CREATOR_FACTORY.addParameter(new SqlParameter(Types.INTEGER));
		INSERT_GOODS_STOCK_PREPARED_STATEMENT_CREATOR_FACTORY.addParameter(new SqlParameter(Types.INTEGER));
		INSERT_GOODS_STOCK_PREPARED_STATEMENT_CREATOR_FACTORY.addParameter(new SqlParameter(Types.TIMESTAMP));
		INSERT_GOODS_STOCK_PREPARED_STATEMENT_CREATOR_FACTORY.addParameter(new SqlParameter(Types.TIMESTAMP));
	}


}
