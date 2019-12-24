package tmall.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import tmall.bean.Category;
import tmall.bean.OrderItem;
import tmall.bean.User;
import tmall.dao.CategoryDAO;
import tmall.dao.OrderItemDAO;

public class ForeServletFilter implements Filter{
	
	@Override
	public void destroy() {
		
	}

	/**
	 * 1.首先在web.xml配置文件中，让所有的请求都会经过ForeServletFilter
	 * 2.还是假设访问路径是 localhost:8079/tmall/forehome
	 * 3.在ForeServlet中通过request.getRequestRUI()取出访问的uri:/tmall/forehome
	 * 4.然后截掉/tamll,得到路径/forehome
	 * 5.判断其是否以/fore开头，并且不是/foreServlet开头
	 * 6.如果是，取出fore之后的值home，并且服务端跳转到foreServlet
	 * 7.在跳转之前，还取出后home字符串，然后通过request.setAttribute的方法，借助服务端跳转，传递到foreServlet里面去
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String contextPath=request.getServletContext().getContextPath();
		request.getServletContext().setAttribute("contextPath", contextPath);
		
		User user =(User) request.getSession().getAttribute("user");
		int cartTotalItemNumber= 0;
		if(null!=user){
			List<OrderItem> ois = new OrderItemDAO().listByUser(user.getId());
			for (OrderItem oi : ois) {
				cartTotalItemNumber+=oi.getNumber();
			}
		}
		request.setAttribute("cartTotalItemNumber", cartTotalItemNumber);
		
		List<Category> cs=(List<Category>) request.getAttribute("cs");
		if(null==cs){
			cs=new CategoryDAO().list();
			request.setAttribute("cs", cs);			
		}
		
		//   /tmall/forehome
		String uri = request.getRequestURI();
		uri =StringUtils.remove(uri, contextPath);
		if(uri.startsWith("/fore")&&!uri.startsWith("/foreServlet")){
			String method = StringUtils.substringAfterLast(uri,"/fore" );
			request.setAttribute("method", method);
			req.getRequestDispatcher("/foreServlet").forward(request, response);
			return;
		}
		
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}
	
	
}
