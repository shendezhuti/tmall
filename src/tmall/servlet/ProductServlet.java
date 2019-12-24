package tmall.servlet;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tmall.bean.Category;
import tmall.bean.Product;
import tmall.bean.Property;
import tmall.bean.PropertyValue;
import tmall.util.Page;

public class ProductServlet extends BaseBackServlet {

	/**
	 * 1.在listProduct.jsp提交数据的时候，除了提交产品名称，小标题，原价格，优惠价格，库存外还会提交cid
	 * 2.在ProductServlet中根据获取到的cid，name，subTitle等参数，创建新的Product对象，并插入到数据库中
	 * 3.客户端跳转到admin_product_list，并带上参数cid
	 */
	public String add(HttpServletRequest request, HttpServletResponse response, Page page) {
		int cid = Integer.parseInt(request.getParameter("cid"));
		Category c = categoryDAO.get(cid);
		String name= request.getParameter("name");
		String subTitle= request.getParameter("subTitle");
		float orignalPrice = Float.parseFloat(request.getParameter("orignalPrice"));
		float promotePrice = Float.parseFloat(request.getParameter("promotePrice"));
		int stock = Integer.parseInt(request.getParameter("stock"));
		Product p = new Product();
		p.setCategory(c);
		p.setName(name);
		p.setSubTitle(subTitle);
		p.setOrignalPrice(orignalPrice);
		p.setPromotePrice(promotePrice);
		p.setStock(stock);
		p.setCreateDate(new Date());
		productDAO.add(p);
		return "@admin_product_list?cid="+cid;
	}

	/**
	 * 1. 在ProductServlet的delete方法中获取id
		2. 根据id获取Product对象
		3. 借助productDAO删除这个对象对应的数据
		4. 客户端跳转到admin_product_list，并带上参数cid
	 */
	public String delete(HttpServletRequest request, HttpServletResponse response, Page page) {
		int id = Integer.parseInt(request.getParameter("id"));
		Product p = productDAO.get(id);
		productDAO.delete(id);
		return "@admin_product_list?cid="+p.getCategory().getId();
	}

	/**
	 * 1.在ProductServlet的edit方法中，根据id获取Prodcut对象
	 * 2.把Product对象放在request的“p”属性中
	 * 3.服务端跳转到admin/editProduct.jsp
	 * 4.在editProduct.jsp中显示属性名称
	 * 5.在editProduct.jsp中隐式提供id和cid
	 */
	public String edit(HttpServletRequest request, HttpServletResponse response, Page page) {
		int id = Integer.parseInt(request.getParameter("id"));
		Product p = productDAO.get(id);
		request.setAttribute("p", p);
		return "admin/editProduct.jsp";		
	}
	
	/**
	 * 1.获取参数id
	 * 2.根据id获取product对象p
	 * 3.初始化属性值：propertyDAO.init(p)。因为如果是第一次访问这些属性值是不存在的
	 * 4.根据Product的id，获取产品对应的属性值集合
	 * 5.属性值集合放在request的“pvs”属性上
	 * 6.服务器跳转到admin/editProductValue.jsp上
	 * 7.在editProductValue.jsp上，用c:forEach遍历出集合
	 * @param request
	 * @param response
	 * @param page
	 * @return
	 */
	public String editPropertyValue(HttpServletRequest request,HttpServletResponse response,Page page){
		int id = Integer.parseInt(request.getParameter("id"));
		Product p = productDAO.get(id);
		request.setAttribute("p", p);
		List<Property> pts= propertyDAO.list(p.getCategory().getId());
	    propertyValueDAO.init(p);
	    List<PropertyValue> pvs = propertyValueDAO.list(p.getId());
	    request.setAttribute("pvs", pvs);
		return "admin/editProduct.jsp";
	}

	/**
	 * 采用post方式提交ajax的异步调用方式
	 * 1.监听输入框上的keyup时间
	 * 2.获取输入框里的值
	 * 3.获取输入框上的自定义属性pvid，这就是当前PropertyValue对应的id
	 * 4.把边框的颜色修改为黄色，表示正在修改的意思
	 * 5.借助JQuery的ajax函数$.post，把id和值，提交到admin_product_updatePropertyValue 
	 * 6.admin_prodcut_updatePropertyValue导致ProdcutServlet的updatePropertyValue方法被调用
	 * 	 6.1获取pvid
	 *   6.2获取value
	 *   6.3基于pvid和value，更新PropertyValue对象
	 *   6.4返回“%success”
	 * 7.BaseBackServlet 根据返回值"%success"，直接输出字符串“success”到秀兰器
	 * 8.浏览器判断如果返回值是“success”，那么就把边框设置为绿色，表示修改成功，否则为红色，表示修改失败
	 * @param request
	 * @param response
	 * @param page
	 * @return
	 */
	
	public String updatePropertyValue(HttpServletRequest request,HttpServletResponse response,Page page){
		int pvid = Integer.parseInt(request.getParameter("pvid"));
        String value = request.getParameter("value");
     
        PropertyValue pv =propertyValueDAO.get(pvid);
        pv.setValue(value);
        propertyValueDAO.update(pv);
        return "%success";
		
	}
	
	/**
	 * 1. 在ProductServlet的update方法中获取cid，id, name,subTitle,price等参数
	   2. 根据这些参数创建Product对象
	   3. 借助productDAO更新这个对象到数据库
	   4. 客户端跳转到admin_product_list，并带上参数cid
	 */
	public String update(HttpServletRequest request, HttpServletResponse response, Page page) {
		int cid = Integer.parseInt(request.getParameter("cid"));
		Category c = categoryDAO.get(cid);

		int id = Integer.parseInt(request.getParameter("id"));
		int stock = Integer.parseInt(request.getParameter("stock"));
		float orignalPrice = Float.parseFloat(request.getParameter("orignalPrice"));
		float promotePrice = Float.parseFloat(request.getParameter("promotePrice"));
		String subTitle= request.getParameter("subTitle");
		String name= request.getParameter("name");
		Product p = new Product();
		p.setName(name);
		p.setSubTitle(subTitle);
		p.setOrignalPrice(orignalPrice);
		p.setPromotePrice(promotePrice);
		p.setStock(stock);
		p.setId(id);
		p.setCategory(c);		
		productDAO.update(p);
		return "@admin_product_list?cid="+p.getCategory().getId();
	}

	/**
	 * 1.获取分类cid
	 * 2.基于cid，获取当前分类下的产品集合
	 * 3.获取当前分类下的产品综述，并设置给分类Page对象
	 * 4.拼接字符串“&cid=”+c.getId(),设置给page对象的Param值。因为产品分页都是基于当前分类下的分页，所以分页的时候需要传递这个cid
	 * 5.把产品集合设置到request的“ps”属性上
	 * 6.把分类对象设置到request的“c”属性上
	 * 7.把分页对象设置到request的“page”对象上
	 * 8.服务器跳转到admin/listProduct.jsp页面
	 * 9.在listProduct.jsp页面使用:forEach遍历ps集合，并显示
	 */
	public String list(HttpServletRequest request, HttpServletResponse response, Page page) {
		int cid = Integer.parseInt(request.getParameter("cid"));
		Category c = categoryDAO.get(cid);
		List<Product> ps = productDAO.list(cid, page.getStart(),page.getCount());
		int total = productDAO.getTotal(cid);
		page.setTotal(total);
		page.setParam("&cid="+c.getId());
		request.setAttribute("ps", ps);
		request.setAttribute("c", c);
		request.setAttribute("page", page);
		return "admin/listProduct.jsp";
	}
}
