package tmall.servlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tmall.bean.Category;
import tmall.bean.Product;
import tmall.bean.Property;
import tmall.bean.PropertyValue;
import tmall.dao.CategoryDAO;
import tmall.util.Page;

public class PropertyServlet extends BaseBackServlet {

	/**
	 * page对象是用来干嘛的呢，完了自己写代码的时候就完全不记得了，好了记起来了Page对象是为了分页我们自己写的
	增加一个属性，我们首先要知道这是什么产品，就要通过cid来获取
	 */
	public String add(HttpServletRequest request, HttpServletResponse response, Page page) {
		int cid = Integer.parseInt(request.getParameter("cid"));
		Category c = categoryDAO.get(cid);
		
		String name= request.getParameter("name");
		Property p = new Property();
		p.setCategory(c);
		p.setName(name);
		propertyDAO.add(p);
		return "@admin_property_list?cid="+cid;
	}

	
	/**
	 * 1.在PropertyServlet的delete方法中获取id
	 * 2.根据id获取Property对象
	 * 3.借助propertyDAO删除这个对象对应的数据
	 * 4.客户端跳转到admin_property_list，并带上参数cid
	 */
	public String delete(HttpServletRequest request, HttpServletResponse response, Page page) {
		int id = Integer.parseInt(request.getParameter("id"));
	    Property p = propertyDAO.get(id);
	    propertyDAO.delete(id);
	    return "@admin_property_list?cid="+p.getCategory().getId();
	}

	/**
	 * 1.根据id获取Property对象
	 * 2.把Property对象放在request的“p”属性中
	 * 3.服务端跳转到admin/editProperty.jsp
	 * 4.在editProperty.jsp中显示属性名称
	 * 5.在editProperty.jsp中隐式提供id和cid
	 */
	public String edit(HttpServletRequest request, HttpServletResponse response, Page page) {
		int id = Integer.parseInt(request.getParameter("id"));
		Property p = propertyDAO.get(id);
		request.setAttribute("p", p);
		return "admin/editProperty.jsp";		
	}
	
	
	
	
	
	/**1.在PropertyServlet的update方法中获取id，cid，name等参数
	 * 2.根据这些参数创建Property对象
	 * 3.借助propertyDAO更新这个对象到数据库
	 * 4.客户端跳转到admin_property_list，带上参数cid
	 * 
	 */
	public String update(HttpServletRequest request, HttpServletResponse response, Page page) {
		int cid = Integer.parseInt(request.getParameter("cid"));
		Category c = categoryDAO.get(cid);
		int id = Integer.parseInt(request.getParameter("id"));
		String name= request.getParameter("name");
		Property p = new Property();
		p.setCategory(c);
		p.setId(id);
		p.setName(name);
		propertyDAO.update(p);
		return "@admin_property_list?cid="+p.getCategory().getId();
	}

	/**
	 * 查询方法首先获取分类cid，基于cid获取当前分类下的属性集合，获取当前分类下的属性总数，并且设置给分页page对象，
	拼接字符串"&cid="+c.getId(),设置给page对象的Param值。因为属性分页都是基于当前分类下的分页，所以分页的时候需要传递这个cid
	把属性集合设置到requst的“ps”属性上
	把分类对象设置到request的"c"属性上
	把分页对象设置到request的"page"对象上
	服务端跳转到admin/listPropery.jsp页面
	在listProperty.jsp页面上使用c:forEach遍历ps集合，并显示
	 */
	public String list(HttpServletRequest request, HttpServletResponse response, Page page) {
		int cid = Integer.parseInt(request.getParameter("cid"));
        Category c = categoryDAO.get(cid);
        List<Property> ps = propertyDAO.list(cid, page.getStart(),page.getCount());
        int total = propertyDAO.getTotal(cid);
        page.setTotal(total);
        page.setParam("&cid="+c.getId());
         
        request.setAttribute("ps", ps);
        request.setAttribute("c", c);
        request.setAttribute("page", page);
         
        return "admin/listProperty.jsp";
	}
}
