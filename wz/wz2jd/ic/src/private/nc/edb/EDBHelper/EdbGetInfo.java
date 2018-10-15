package nc.edb.EDBHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import nc.wznc2jd.JDHelper.CommonHelper;

public class EdbGetInfo {

	/**
	 * 添加入库单
	 * 
	 */
	public static String edbInStoreAdd(nc.vo.pu.m23.entity.ArriveVO data) {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb.edbGetCommonParams("edbInStoreAdd");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String storCode = "";
		StringBuilder builder = new StringBuilder();
		builder.append("<info>");
		builder.append("<orderInfo>");
		builder.append("<instorage_no>" + data.getHVO().getVbillcode()
				+ "</instorage_no>");// 发货单号
		builder.append("<instorage_type>正常入库</instorage_type>");// 正常入库
		builder.append("<instorage_time>" + df.format(new Date())
				+ "</instorage_time>");// 当前时间
		for (int i = 0; i < data.getBVO().length; i++) {
			storCode = CommonHelper.GetWarecode(data.getBVO()[0]
					.getPk_receivestore());
		}
		builder.append("<storage_no>" + storCode + "</storage_no>");// 仓库档案上自定义项值2
		builder.append("<supplier_no>7</supplier_no>");// 直接用默认值：7 ；名称：百E
		builder.append("<instorage_reason>采购入库</instorage_reason>");// 默认：采购入库
		String menoh = data.getHVO().getVmemo();
		builder.append("<inStorage_remark>" + menoh + "</inStorage_remark>");// 到货单：表头备注
		builder.append("<import_sign>未导入</import_sign>");// 默认值;未导入
		builder.append("<Source_no>" + data.getHVO().getVbillcode()
				+ "</Source_no>");// 到货单号
		builder.append("<tax>16</tax>");// 默认：16
		builder.append("</orderInfo>");

		for (int i = 0; i < data.getBVO().length; i++) {

			builder.append("<product_info>");
			builder.append("<product_item>");
			builder.append("<instorage_no>" + data.getHVO().getVbillcode()
					+ "</instorage_no>");// NC每个账套到货单号前置信息不一致 到货单号
			// builder.append("<cost>" + data.getBVO()[i].getNtaxprice()+
			// "</cost>");// 到货单，本币含税单价
			String code = CommonHelper.GetMaterialcode(data.getBVO()[i]
					.getPk_srcmaterial());
			builder.append("<productItem_no>" + code + "</productItem_no>");// 物料基本编码
			builder.append("<instorage_num>" + data.getBVO()[i].getNastnum()
					+ "</instorage_num>"); // 到货数量

			builder.append("<storage_no>" + storCode + "</storage_no>");// 仓库自定义项2
			builder.append("<instorage_remark>'" + data.getBVO()[i].getVmemob()
					+ "'</instorage_remark>");// 行备注

			String materialbarcode = CommonHelper.GetMaterialbarcode(data
					.getBVO()[i].getPk_srcmaterial());

			builder.append("<bar_code>" + materialbarcode + "</bar_code>");// 物料档案：条形码
			builder.append("</product_item>");
			builder.append("</product_info>");
		}
		builder.append("</info>");

		params.put("xmlValues", builder.toString());
		String res = edb.edbRequstPost(params);
		return res;
	}

	/**
	 * 获取订单
	 */
	public static String edbInStoreGet(String vbillcode) {
		// date_type、begin_time、end_time三个配合使用，比如
		// date_type=发货日期，begin_time=2017-05-01 00:00:00,end_time=2017-07-01
		// 00:00:00 表示查询发货日期在2017-05-01 00:00:00与2017-07-01 00:00:00之间的订单
		// 查询时符合条件的单据可能有很多条，为了获取所有符合条件的单据:
		// page_no从1开始查，若返回的单据个数（json的话item数组的长度；xml的话Rows标签的个数）等于page_size,则递增page_no接着查，直到返回的订单个数小于page_size
		EdbLib edb = new EdbLib();
		Calendar calendar = Calendar.getInstance();
		// HOUR_OF_DAY 指示一天中的小时
		calendar.set(Calendar.HOUR_OF_DAY,
				calendar.get(Calendar.HOUR_OF_DAY) - 1);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("一个小时前的时间：" + df.format(calendar.getTime()));
		System.out.println("当前的时间：" + df.format(new Date()));
		Map<String, String> params = edb.edbGetCommonParams("edbInStoreGet");
		params.put("type", "8");// 正常入库
		params.put("date_type", "in_time");// 日期类型--入库日期in_time,制单日期create_time,审核日期examine_time
		//params.put("begin_time", df.format(calendar.getTime()));
		params.put("begin_time", "2018-10-1 00:00:00");
		params.put("end_time", df.format(new Date()));
		//params.put("in_store_tid", vbillcode);
		params.put("in_store_tid", "r18100500035");//订单号
		params.put("status", "1");
		// params.put("page_no", "1");
		// params.put("order_status", "已发货");
		// params.put("page_size", "10");
		// params.put("payment_status", "已付款");
		// params.put("productInfo_type", "3");
		// params.put("fields",
		// "storage_id,tid,is_bill,invoice_name,taobao_delivery_status,taobao_delivery_method,order_process_time,is_break,breaker,break_time,break_explain,plat_send_status,plat_type,is_adv_sale,provinc_code,city_code,area_code,express_code,last_returned_time,last_refund_time,deliver_centre,deliver_station,is_pre_delivery_notice,jd_delivery_time,Sorting_code,cod_settlement_vouchernumber,total_num,big_marker,three_codes,distributing_centre_name,send_site_name,:child_storage_id,:child_tid,:child_out_tid,:child_pro_detail_code,:child_pro_name,:child_specification,:child_barcode,:child_combine_barcode,:child_iscancel,:child_isscheduled,:child_stock_situation,:child_isbook_pro,:child_iscombination,:child_isgifts,:child_gift_num,:child_book_storage,:child_pro_num,:child_send_num,:child_refund_num,:child_refund_renum,:child_inspection_num,:child_timeinventory,:child_cost_price,:child_sell_price");
		String res = edb.edbRequstPost(params);
		return res;
	}

	/**
	 * 添加入库单
	 */
	public static void edbInStoreAdd() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb.edbGetCommonParams("edbInStoreAdd");
		StringBuilder builder = new StringBuilder();
		builder.append("<info>");
		builder.append("<orderInfo>");
		builder.append("<instorage_no>20549778</instorage_no>");
		builder.append("<instorage_type>正常入库</instorage_type>");
		builder.append("<instorage_time>2016-10-20 09:54</instorage_time>");
		builder.append("<storage_no>1</storage_no>");
		builder.append("<supplier_no>1</supplier_no>");
		builder.append("<delivery_no>EMS1265489</delivery_no>");
		builder.append("<cost>20</cost>");
		builder.append("<procure_cost>10</procure_cost>");
		builder.append("<other_cost>5</other_cost>");
		builder.append("<pact_totalAmount>100</pact_totalAmount>");
		builder.append("<out_pactNo>cs390233837</out_pactNo>");
		builder.append("<WL_company>无菌物流</WL_company>");
		builder.append("<express_no>1235345345</express_no>");
		builder.append("</orderInfo>");

		builder.append("<orderInfo>");
		builder.append("<instorage_no>20549779</instorage_no>");
		builder.append("<instorage_type>正常入库</instorage_type>");
		builder.append("<instorage_time>2016-10-20 09:54</instorage_time>");
		builder.append("<storage_no>1</storage_no>");
		builder.append("<supplier_no>1</supplier_no>");
		builder.append("<delivery_no>EMS1265489</delivery_no>");
		builder.append("<cost>20</cost>");
		builder.append("<procure_cost>10</procure_cost>");
		builder.append("<other_cost>5</other_cost>");
		builder.append("<pact_totalAmount>100</pact_totalAmount>");
		builder.append("<out_pactNo>cs390233837</out_pactNo>");
		builder.append("<WL_company>无菌物流</WL_company>");
		builder.append("<express_no>1235345345</express_no>");
		builder.append("</orderInfo>");

		builder.append("<product_info>");
		builder.append("<product_item>");
		builder.append("<instorage_no>20549778</instorage_no>");
		builder.append("<productItem_no>1</productItem_no>");
		builder.append("<instorage_num>10</instorage_num>");
		builder.append("<storage_no>1</storage_no>");
		builder.append("<batch>10000</batch>");
		builder.append("<expire_Time>2017-06-20</expire_Time>");
		builder.append("</product_item>");

		builder.append("<product_item>");
		builder.append("<instorage_no>20549779</instorage_no>");
		builder.append("<productItem_no>2</productItem_no>");
		builder.append("<instorage_num>2</instorage_num>");
		builder.append("<storage_no>1</storage_no>");
		builder.append("<batch>100001</batch>");
		builder.append("<expire_Time>2017-06-01</expire_Time>");
		builder.append("</product_item>");

		builder.append("</product_info>");
		builder.append("</info>");

		params.put("xmlValues", builder.toString());
		String res = edb.edbRequstPost(params);

	}

	/**
	 * 添加订单
	 */
	public static void edbTradeAdd() {

		EdbLib edb = new EdbLib();
		Map<String, String> params = edb.edbGetCommonParams("edbTradeAdd");
		StringBuilder builder = new StringBuilder();
		builder.append("<info>");
		builder.append("<orderInfo>");
		builder.append("<out_tid>WZ2017121900003</out_tid>");// 外部平台单号（平台订单编号），很重要，请唯一，尽量建立自己的与其他平台不重复的编码体系，那些12345就是极不用心，极不规范的，你比如你的平台叫：爱我吧，编号规则：AO201605090000001，这样多有追求
		builder.append("<shop_id>17</shop_id>");// 店铺编号（E店宝客户端档案管理→基本档案→店铺设置中查看），如果没有取appkey绑定的店铺
		builder.append("<storage_id>4</storage_id>");// 仓库编码（E店宝客户端档案管理→仓库档案→仓库设置中查看），如果没有取关联店铺的默认仓库
		builder.append("<buyer_id>中原一点红</buyer_id>");// 买家ID，在你网站注册的账号名称，如果没有填收货人吧
		builder.append("<buyer_msg>,9.28云集廊坊仓安慕希黄桃+燕麦出库PO201709280103</buyer_msg>");// 买家留言
		builder.append("<seller_remark/>");// 客服备注
		builder.append("<consignee><![CDATA[张<三]]></consignee>");// 收货人真实姓名
		builder.append("<telephone>13288888888</telephone>");// 联系电话
		builder.append("<mobilPhone>13288888888</mobilPhone>");// 联系手机（手机和电话须有一个不为空）
		builder.append("<privince>陕西省</privince>");// 收货人省份
		builder.append("<city>西安市</city>");// 收货人城市
		builder.append("<area>高新区</area>");// 收货人地区
		builder.append("<address>陕西省西安市高新区丈八五路46号长盛科技产业园</address>");// 收货地址，请包含省市区
		builder.append("<actual_freight_get>0</actual_freight_get>");// 实收运费，代收买家的运费，货到付款就是0
		builder.append("<is_COD>0</is_COD>");// 是否货到付款，是的话一定要填1，不然损失惨重
		builder.append("<order_totalMoney>138</order_totalMoney>");// 订单总金额，买家实际支付的金额，假如买家花80买了一张100抵扣券，这80要算进去，假如他用你赠送的30元抵扣券，这30不要算进去，就是你这笔生意一共实收的金额，不要算错账
		builder.append("<product_totalMoney>138</product_totalMoney>");// 产品总金额，实收的产品总金额，不包含运费，与下面产品的sum（订货数量*成交单价）对应
		builder.append("<favorable_money>0</favorable_money>");// 优惠金额:订单总优惠金额，产品上的优惠不包含在内，比如一个订单一个产品999，打折680，现在五一搞特价只要499，319（999-680）放下面的优惠上，181（680-499）放这里
		builder.append("<terminal_type>手机</terminal_type>");// 终端类型(电脑-手机-电话)，用来按终端类型促销的，软件可以设置对指定类型的订单开启赠送
		builder.append("<pay_method>微信支付</pay_method>");// 支付方式，支付宝，财递通，现金等
		builder.append("<out_payNo>20549778_02081654</out_payNo>");// 外部平台付款单号，就是支付的流水号，一般采用第三方支付接口都会能得到这个流水号，方便查账
		builder.append("<pay_date>2017-12-19 08:16</pay_date>");// 付款日期
		builder.append("<order_date>2017-12-19 08:16</order_date>");// 订货日期（订货日期距当前时间不可超过一个月）
		// builder.append("<express>韵达</express>");//快递公司名称（E店宝客户端档案管理→仓库档案→快递公司设置查看），如果不填写取关联店铺绑定的默认快递
		builder.append("<pay_status>已付款</pay_status>");// 付款状态（交易关闭,未付款（有个参数控制不导入未付款单，需要开启，导入成功的订单找不到别怪我没告诉你）,已付款,交易完成,已发货,预退款）默认：未付款，如果是购买后发生了退款/退货情况，此状态就填预退款，发生退货的产品标题上加上【退款产品】这个标记，到软件中会变成待退款部分退款/待退款全部退款，这种订单在仓库作业环节就可以拦截了
		builder.append("<invoice_type>个人</invoice_type>");// 发票类型
		builder.append("<invoice_title>发票抬头</invoice_title>");// 发票抬头（这里填写后到软件中显示是否开发票√）
		builder.append("<invoice_msg>发票内容</invoice_msg>");// 发票内容
		builder.append("</orderInfo>");

		/*
		 * builder.append("<orderInfo>");
		 * builder.append("<out_tid>WZ2017080800006</out_tid>");
		 * builder.append("<shop_id>16</shop_id>");
		 * builder.append("<storage_id>25</storage_id>");
		 * builder.append("<buyer_id>王菁雯</buyer_id>");
		 * builder.append("<buyer_msg>【发件人: 韦林红】要求西安发货，麻烦确保质量，谢谢</buyer_msg>");
		 * builder.append("<seller_remark/>");
		 * builder.append("<consignee>王菁雯</consignee>");
		 * builder.append("<telephone>13588217105</telephone>");
		 * builder.append("<privince></privince>");
		 * builder.append("<city></city>"); builder.append("<area></area>");
		 * builder.append("<address>陕西省西安市高新区丈八五路46号长盛科技产业园</address>");
		 * builder.append("<actual_freight_get>0</actual_freight_get>");
		 * builder.append("<is_COD>0</is_COD>");
		 * builder.append("<order_totalMoney>138</order_totalMoney>");
		 * builder.append("<product_totalMoney>138</product_totalMoney>");
		 * builder.append("<is_invoiceOpened>0</is_invoiceOpened>");
		 * builder.append("<favorable_money>0</favorable_money>");
		 * builder.append("<terminal_type>微信</terminal_type>");
		 * builder.append("<pay_method>微信支付</pay_method>");
		 * builder.append("<out_payNo>20549778_02081653</out_payNo>");
		 * builder.append("<pay_date>2017-09-21 08:16</pay_date>");
		 * builder.append("<order_date>2017-09-21 08:16</order_date>");
		 * builder.append("<express>韵达</express>");
		 * builder.append("<pay_status>已付款</pay_status>");
		 * builder.append("</orderInfo>");
		 */
		builder.append("<product_info>");
		builder.append("<product_item>");
		// 测试条码YST100,SYJ100
		builder.append("<barCode>911911888308057</barCode>");// 条形码，用来对应到软件中预置的产品，方便后续的配货、发货，条形码填写错误会进软件，但会进异常单处理，接口无法查出来，说订单找不到不妨去异常单看下
		builder.append("<product_title>兄弟鸭舌，真正宗的武汉原味，正品信赖二十年久远</product_title>");// 商品标题
		builder.append("<standard>袋</standard>");// 商品属性，一般是sku描述，比如XL红色、L绿色，没有可默认均码、个、只等量词
		builder.append("<favorite_money>0</favorite_money>");// 优惠金额:单品的优惠金额，注意是该条产品的总优惠金额，而非一件的优惠
		builder.append("<orderGoods_Num>1</orderGoods_Num>");// 订货数量
		builder.append("<cost_Price>46</cost_Price>");// 成交单价
		builder.append("<out_tid>WZ2017121900003</out_tid>");// 对应orderInfo中的out_tid表示订单明细
		builder.append("</product_item>");

		/*
		 * builder.append("<product_item>");
		 * builder.append("<out_tid>WZ2017080800005</out_tid>"
		 * );//对应orderInfo中的out_tid表示订单明细
		 * builder.append("<barCode>CP12012GG2</barCode>"); builder.append(
		 * "<product_title>油炸鲨鱼鲞你见过吗，没见过就点进来尝一尝看一看没准就新欢</product_title>");
		 * builder.append("<standard>五香（4袋）X1份</standard>");
		 * builder.append("<favorite_money>0</favorite_money>");
		 * builder.append("<orderGoods_Num>1</orderGoods_Num>");
		 * builder.append("<cost_Price>46</cost_Price>");
		 * builder.append("</product_item>");
		 * 
		 * builder.append("<product_item>");
		 * builder.append("<barCode>911911888308057</barCode>");
		 * builder.append("<product_title>兄弟鸭舌，真正宗的武汉原味，正品信赖二十年久远</product_title>"
		 * ); builder.append("<standard>麻辣（4袋）X2份</standard>");
		 * builder.append("<favorite_money>0</favorite_money>");
		 * builder.append("<orderGoods_Num>1</orderGoods_Num>");
		 * builder.append("<cost_Price>46</cost_Price>");
		 * builder.append("<out_tid>WZ2017080800006</out_tid>"
		 * );//对应orderInfo中的out_tid表示订单明细 builder.append("</product_item>");
		 * builder.append("<product_item>");
		 * builder.append("<out_tid>WZ2017080800006</out_tid>"
		 * );//对应orderInfo中的out_tid表示订单明细
		 * builder.append("<barCode>CP12012GG2</barCode>"); builder.append(
		 * "<product_title>油炸鲨鱼鲞你见过吗，没见过就点进来尝一尝看一看没准就新欢</product_title>");
		 * builder.append("<standard>五香（4袋）X1份</standard>");
		 * builder.append("<favorite_money>0</favorite_money>");
		 * builder.append("<orderGoods_Num>1</orderGoods_Num>");
		 * builder.append("<cost_Price>46</cost_Price>");
		 * builder.append("</product_item>");
		 */
		builder.append("</product_info>");
		builder.append("</info>");

		params.put("xmlValues", builder.toString());
		String res = edb.edbRequstPost(params);
		// String res = edb.name(params);
	}

	/**
	 * 订单更新
	 */
	public static void edbTradeUpdate() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb.edbGetCommonParams("edbTradeUpdate");
		StringBuilder builder = new StringBuilder();
		builder.append("<info>");
		builder.append("<orderInfo>");
		builder.append("<tid>S1611110013488</tid>");
		builder.append("<express>圆通快递</express>");
		builder.append("<express_no>809304336767</express_no>");
		builder.append("<printer>edb_a80258</printer>");
		builder.append("<print_time>2016-11-12 10:50:55</print_time>");
		builder.append("<inspecter>edb_a80258</inspecter>");
		builder.append("<inspect_time>2016-11-12 10:50:55</inspect_time>");
		builder.append("<delivery_operator>edb_a80258</delivery_operator>");
		builder.append("<delivery_time>2016-11-12 10:50:55</delivery_time>");
		builder.append("<GrossWeight>1.0</GrossWeight>");
		builder.append("</orderInfo>");

		builder.append("<product_info>");
		builder.append("<product_item>");
		builder.append("<tid>S1611110013488</tid>");
		builder.append("<barCode>34210531064402</barCode>");
		builder.append("<inspection_num>1</inspection_num>");
		builder.append("</product_item>");

		builder.append("<product_item>");
		builder.append("<tid>S1611110013488</tid>");
		builder.append("<barCode>44210530960602</barCode>");
		builder.append("<inspection_num>1</inspection_num>");
		builder.append("</product_item>");
		builder.append("</product_info>");
		builder.append("</info>");

		params.put("xmlValues", builder.toString());
		String res = edb.edbRequstPost(params);
		// String res = edb.name(params);
	}

	/**
	 * 订单批量发货
	 */
	public static void edbTradeDeliveryBatch() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbTradeDeliveryBatch");
		StringBuilder builder = new StringBuilder();
		builder.append("<info>");
		// 第1个订单
		builder.append("<orderInfo>");
		builder.append("<OrderCode>S1611110013488</OrderCode>");
		builder.append("<express>圆通快递</express>");
		builder.append("<express_no>809304336767</express_no>");
		builder.append("<delivery_time>2016-11-12 10:50:55</delivery_time>");
		builder.append("<weight>1.0</weight>");
		builder.append("</orderInfo>");
		// 第2个订单
		builder.append("<orderInfo>");
		builder.append("<OrderCode>S1611110013489</OrderCode>");
		builder.append("<express>圆通快递</express>");
		builder.append("<express_no>809304336768</express_no>");
		builder.append("<delivery_time>2016-11-12 10:50:55</delivery_time>");
		builder.append("<weight>1.0</weight>");
		builder.append("</orderInfo>");

		builder.append("</info>");

		params.put("xmlValues", builder.toString());
		String res = edb.edbRequstPost(params);
	}

	/**
	 * 添加盘点单
	 */
	public static void edbInventoryAdd() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb.edbGetCommonParams("edbInventoryAdd");
		StringBuilder builder = new StringBuilder();
		builder.append("<info>");
		builder.append("<orderInfo>");
		builder.append("<checkOrderCode>PD23659</checkOrderCode>");
		builder.append("<storage_no>30</storage_no>");
		builder.append("<checkTime>2016-11-12 10:50:55</checkTime>");
		builder.append("<checkType>年中盘点</checkType>");
		builder.append("<remark>盘点说明</remark>");
		builder.append("</orderInfo>");

		builder.append("<product_info>");
		builder.append("<product_item>");
		builder.append("<checkOrderCode>PD23659</checkOrderCode>");
		builder.append("<barCode>ALB015A</barCode>");
		builder.append("<quantity>1</quantity>");
		builder.append("</product_item>");

		builder.append("<product_item>");
		builder.append("<checkOrderCode>PD23659</checkOrderCode>");
		builder.append("<barCode>ALB016A</barCode>");
		builder.append("<quantity>1</quantity>");
		builder.append("</product_item>");
		builder.append("</product_info>");
		builder.append("</info>");

		params.put("xmlValues", builder.toString());
		String res = edb.edbRequstPost(params);
		// String res = edb.name(params);
	}

	/**
	 * 快递更新（已过时）
	 */
	public static void edbExpressUpdate() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb.edbGetCommonParams("edbExpressUpdate");
		StringBuilder builder = new StringBuilder();
		builder.append("<info>");
		builder.append("<orderInfo>");
		builder.append("<Orderid>S1707270002564</Orderid>");
		builder.append("<Express>2</Express>");
		builder.append("<storage_id>10</storage_id>");
		builder.append("<Csmemo>大猫测试</Csmemo>");
		builder.append("</orderInfo>");
		builder.append("</info>");

		params.put("xmlValues", builder.toString());
		String res = edb.edbRequstPost(params);
		// String res = edb.name(params);
	}

	/**
	 * 订单作废
	 */
	public static void edbTradeCancel() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb.edbGetCommonParams("edbTradeCancel");
		StringBuilder builder = new StringBuilder();
		builder.append("<order>");
		builder.append("<orderInfo><tid>S1709090055178</tid></orderInfo>");// tid
																			// 是
																			// EDB订单编号，不是外部订单号
		// builder.append("<orderInfo><tid>S1212030600004</tid></orderInfo>");
		builder.append("</order>");
		params.put("xmlValues", builder.toString());

		String res = edb.edbRequstPost(params);
		String s = "";
	}

	/**
	 * 获取退货订单信息
	 */
	public static void edbTradReturnGet() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb.edbGetCommonParams("edbTradReturnGet");
		// params.put("date_type", "退货到货日期");
		params.put("start_time", "20160101");
		params.put("end_time", "20160928");
		// params.put("import_mark", "未导入");
		// params.put("process_status", "处理中");
		// params.put("return_status", "退货中");
		// params.put("refund_status", "未退款");
		params.put("page_no", "1");
		params.put("page_size", "5");
		String res = edb.edbRequstPost(params);
	}

	/**
	 * 获取发票信息
	 */
	public static void edbInvoiceGet() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb.edbGetCommonParams("edbInvoiceGet");
		// params.put("begin_time", "2015-07-29 23:24:40");
		// params.put("end_time", "2015-07-29 23:24:40");
		// params.put("page_no", "1");
		// params.put("page_size", "5");
		String res = edb.edbRequstPost(params);
	}

	/**
	 * 增加产品明细信息
	 */
	public static void edbProductDetailAdd() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbProductDetailAdd");
		StringBuilder builder = new StringBuilder();
		builder.append("<info>");
		builder.append("<orderInfo>");
		builder.append("<brand_name>麦富迪</brand_name>");
		builder.append("<sort_name>猫湿粮SUNNY PATTAYA阳光芭堤亚</sort_name>");
		builder.append("<supplier>山东麦富迪贸易发展有限公司</supplier>");
		// builder.append("<productNo>LWGW00010</productNo>");
		builder.append("<product_name>阳光芭堤亚浓汁吞拿鱼+鸡肉</product_name>");
		builder.append("<market_price>0</market_price>");
		builder.append("<retail_price>0</retail_price>");
		builder.append("<product_intro>1</product_intro>");
		builder.append("<factory_item>1</factory_item>");
		builder.append("<wfpid>192.168.0.10</wfpid>");
		builder.append("</orderInfo>");

		builder.append("<detailInfo>");
		builder.append("<detail_item>");
		builder.append("<bar_code>6958862109529</bar_code>");
		builder.append("<specification>60g</specification>");
		// builder.append("<color>袋</color>");
		builder.append("<size>1.0</size>");
		builder.append("<unit>袋</unit>");
		builder.append("<weight>0.06</weight>");
		builder.append("<contrast_purchase_price>1.0</contrast_purchase_price>");
		// builder.append("<product_status>正常</product_status>");
		builder.append("<sell_price>0</sell_price>");

		builder.append("</detail_item>");
		builder.append("</detailInfo>");
		builder.append("</info>");

		params.put("xmlValues", builder.toString());
		String res = edb.edbRequstPost(params);

	}

	/**
	 * 获取产品基本产品信息 根据产品启用的开始结束时间和产品条码，编号等信息获取产品基本信息，不包括库存
	 */
	public static void edbProductBaseInfoGet() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbProductBaseInfoGet");
		params.put("BarCode", "Edb0092");

		String res = edb.edbRequstPost(params);
		String s = "";
	}

	/**
	 * 获取E店宝启用的品牌信息
	 */
	public static void edbProductBrandGet() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbProductBrandGet");
		// params.put("brand_code", "W60508");
		// params.put("brand_name", "W60508");
		// params.put("enable", "W60508");

		String res = edb.edbRequstPost(params);
		String s = "";
	}

	/**
	 * 获取产品分类信息
	 */
	public static void edbProductClassGet() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbProductClassGet");
		// params.put("class_code", "98");//EDB系统分类编号
		params.put("class_name", "食品生鲜");
		// params.put("sort_type", "产品");
		// params.put("is_suit", "0");
		// params.put("store_name", "ETOP顺丰仓");
		// params.put("get_child_nodes", "true");
		String res = edb.edbRequstPost(params);
		String s = "";
	}

	/**
	 * 获取产品库存信息 没有按库存变动时间查询的方法。一般业务下每隔5~10分钟需要查询一遍所有商品的库存
	 */
	public static void edbProductGet() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb.edbGetCommonParams("edbProductGet");
		params.put("date_type", "建档日期");// 这个参数影响begin_time 和 end_time
		params.put("begin_time", "2012-01-01 00:00"); // date_type
														// 大于等于begin_time
		params.put("end_time",
				new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));// date_type
																				// 大于等于end_time
		// params.put("product_no", "PN000036");
		// params.put("brand_no", "28"); //EDB系统品牌编号
		// params.put("standard", "规格");
		params.put("bar_code", "SKYWORTH-00000030");
		// params.put("sort_no", "96"); // EDB系统分类编号
		// params.put("store_id", "1,2,3");
		// params.put("isuit", "0");
		// params.put("iscut_store", "1");
		params.put("page_no", "1");
		params.put("page_size", "10");
		String res = edb.edbRequstPost(params);
		String s = "";
	}

	/**
	 * 更新主产品信息
	 */
	public static void edbProductBaseInfoUpdate() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbProductBaseInfoUpdate");
		params.put("productId", "436");
		params.put("brand_name", "天元");
		params.put("productNo", "LWH-0006");
		params.put("product_name", "软底鞋");
		String res = edb.edbRequstPost(params);
		String s = "";
	}

	/**
	 * 更新单据导入标记为 已导入
	 */
	public static void edbTradeImportStatusUpdate() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbTradeImportStatusUpdate");
		params.put("num_id", "S1512020000001,S1512020000002:批注");
		params.put("tid_type", "Order");

		String res = edb.edbRequstPost(params);
		String s = "";
	}

	/**
	 * 更新物流信息
	 */
	public static void edbLogisticsCompaniesUpdate() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbLogisticsCompaniesUpdate");
		params.put("tid", "S1512020000001");// E店宝订单号
		params.put("express", "圆通快递");
		params.put("express_deliveryno", "809173667727");
		params.put("delivery_time", "2016-11-13 18:20:10");
		params.put("weight", "0");

		String res = edb.edbRequstPost(params);
		String s = "";
	}

	/**
	 * 增加一个产品分类
	 */
	public static void edbProductClassAdd() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbProductClassAdd");
		StringBuilder builder = new StringBuilder();
		builder.append("<info>");
		builder.append("<orderInfo>");
		builder.append("<sort_name>终端品</sort_name>");
		builder.append("<OutSortNO>HC-037</OutSortNO>");
		builder.append("<sort_type>产品</sort_type>");
		builder.append("</orderInfo>");
		builder.append("</info>");

		params.put("xmlValues", builder.toString());
		String res = edb.edbRequstPost(params);
		String s = "";
	}

	/**
	 * 增加一个产品品牌
	 */
	public static void edbProductBrandAdd() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbProductBrandAdd");
		StringBuilder builder = new StringBuilder();
		builder.append("<order>");
		builder.append("<orderInfo>");
		builder.append("<brand_name>Zimmerli齐穆里2016</brand_name>");// 品牌名称
		builder.append("<OutBrandNO>Z005</OutBrandNO>");
		builder.append("<Cdescript></Cdescript>");
		builder.append("<Cremark></Cremark>");
		builder.append("<Is_active>1</Is_active>");
		builder.append("</orderInfo>");
		builder.append("</order>");

		params.put("xmlValues", builder.toString());
		String res = edb.edbRequstPost(params);
		String s = "";
	}

	/**
	 * 更新产品分类
	 */
	public static void edbProductClassUpdate() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbProductClassUpdate");
		params.put("sort_name", "S1512020000001");
		params.put("sort_no", "10");
		// params.put("parent_sort_no", "Order");
		params.put("packing_charges", "1");
		params.put("rough_weight_ratio", "0");
		params.put("sort_type ", "产品");
		params.put("storeId ", "2");
		params.put("is_pack ", "0");
		params.put("is_suit ", "0");
		String res = edb.edbRequstPost(params);
		String s = "";
	}

	/**
	 * 退货单导入接口
	 */
	public static void edbReturnStoreAdd() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbReturnStoreAdd");
		StringBuilder builder = new StringBuilder();
		builder.append("<corder>");
		builder.append("<corderInfo>");
		builder.append("<ordernum>20549778</ordernum>");
		builder.append("<wresingnum>89642354865122</wresingnum>");
		builder.append("<stoaffirm>0</stoaffirm>");
		builder.append("<expressnum>981632</expressnum>");
		builder.append("<expresscom>圆通</expresscom>");
		builder.append("<retime>2017-06-10 10:00</retime>");
		builder.append("</corderInfo>");

		builder.append("<corderInfo>");
		builder.append("<ordernum>20549779</ordernum>");
		builder.append("<wresingnum>89642354865123</wresingnum>");
		builder.append("<stoaffirm>0</stoaffirm>");
		builder.append("<expressnum>981632</expressnum>");
		builder.append("<expresscom>圆通</expresscom>");
		builder.append("<retime>2017-06-10 10:00</retime>");
		builder.append("</corderInfo>");

		builder.append("<rproductInfo>");
		builder.append("<rproduct_item>");
		builder.append("<barcode>20549778</barcode>");
		builder.append("<wresingnum>89642354865122</wresingnum>");
		builder.append("<pronum>1</pronum>");
		builder.append("<reamount>100.00</reamount>");
		builder.append("</rproduct_item>");

		builder.append("<rproduct_item>");
		builder.append("<barcode>20549778</barcode>");
		builder.append("<wresingnum>89642354865123</wresingnum>");
		builder.append("<pronum>1</pronum>");
		builder.append("<reamount>120.00</reamount>");
		builder.append("</rproduct_item>");

		builder.append("</rproductInfo>");
		builder.append("</corder>");

		params.put("xmlValues", builder.toString());
		String res = edb.edbRequstPost(params);

	}

	/**
	 * 确认入库单
	 */
	public static void edbInStoreConfirm() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbInStoreConfirm");
		params.put("inStorage_no", "7026899788-702689996");
		params.put("freight", "5.00");

		String res = edb.edbRequstPost(params);
		String s = "";
	}

	/**
	 * 订单导入待发货
	 */
	public static void edbReadySynDeliveries() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbReadySynDeliveries");
		StringBuilder builder = new StringBuilder();
		builder.append("<order>");
		builder.append("<orderInfo>");
		builder.append("<order_no>SO1708070002056</order_no>");
		builder.append("<express>韵达</express>");
		builder.append("<express_no>SO1708070002056</express_no>");
		builder.append("<consignee>王菁雯</consignee>");
		builder.append("<delivery_time>2017-08-08 10:00</delivery_time>");
		builder.append("<package_num>1</package_num>");
		builder.append("</orderInfo>");

		// builder.append("<orderInfo>");
		// builder.append("<order_no>SO1708080001778</order_no>");
		// builder.append("<express>韵达</express>");
		// builder.append("<express_no>SO1708080001778</express_no>");
		// builder.append("<consignee>王菁雯</consignee>");
		// builder.append("<delivery_time>2017-08-08 14:00</delivery_time>");
		// builder.append("<package_num>1</package_num>");
		// builder.append("</orderInfo>");

		builder.append("</order>");

		params.put("xmlValues", builder.toString());
		String res = edb.edbRequstPost(params);

	}

	/*
	 * 查询待发货订单信息
	 */
	public static void edbGetSynDeliveries() {
		// 查询时符合条件的单据可能有很多条，为了获取所有符合条件的单据:
		// page_no从1开始查，若返回的单据个数（json的话item数组的长度；xml的话Rows标签的个数）等于page_size,则递增page_no接着查，直到返回的订单个数小于page_size

		EdbLib edb = new EdbLib();
		Map<String, String> params = edb
				.edbGetCommonParams("edbGetSynDeliveries");
		params.put("begin_time", "2017-01-01");
		params.put("end_time", "2017-08-15");
		params.put("page_no", "1");
		params.put("page_size", "200");
		// params.put("order_nos",
		// "S1512020000110,S1702080001139");//EDB单号(总长度2000个字符)EDB单号与平台单号都传时只取EDB单号
		params.put("out_order_nos", "99998887776777,170206-356617403521");// 平台单号(总长度2000个字符)EDB单号与平台单号都传时只取EDB单号
		// params.put("fields", "order_no,out_order_no");//指定返回字段
		String res = edb.edbRequstPost(params);

	}

	// / <summary>
	// / 通知订单
	// / </summary>
	private static void edbNotifyTrade() {
		EdbLib edb = new EdbLib();
		Map<String, String> params = edb.edbGetCommonParams("edbNotifyTrade");
		StringBuilder builder = new StringBuilder();
		builder.append("<info>");
		builder.append("<orderInfo>");
		builder.append("<bizOrderId>WZ201804270001</bizOrderId>");// 好衣库单号
		builder.append("<bizOrderPushType>部分退款</bizOrderPushType>");// 订单推送类型
		builder.append("</orderInfo>");
		// 第2单
		builder.append("<orderInfo>");
		builder.append("<bizOrderId>WZ201804270002</bizOrderId>");// 好衣库单号
		builder.append("<bizOrderPushType>地址变更</bizOrderPushType>");// 订单推送类型
		builder.append("</orderInfo>");
		builder.append("</info>");
		params.put("xmlValues", builder.toString());
		String res = edb.edbRequstPost(params);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// edbReadySynDeliveries();
		// edbGetSynDeliveries();
		// edbExpressUpdate();
		// edbProductGet();
		edbTradeAdd();
		// edbInStoreAdd();
		// edbTradeGet();
		// edbTradReturnGet();
		// edbInvoiceGet();//获取发票信息
		// edbProductBaseInfoGet();
		// edbTradeImportStatusUpdate();
		// edbProductClassAdd();
		// edbProductClassGet();//获取产品分类信息
		// edbProductClassUpdate();
		// edbProductDetailAdd();
		// edbInStoreConfirm();
		// edbTradeCancel();
		// edbLogisticsCompaniesUpdate();
		// edbInventoryAdd();
		// edbProductBrandGet();
		// edbTradeDeliveryBatch();
		// edbReturnStoreAdd();
		// edbProductBrandAdd();
	}

}
