package tmall.servlet;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.web.util.HtmlUtils;

import tmall.bean.Category;
import tmall.bean.Order;
import tmall.bean.OrderItem;
import tmall.bean.Product;
import tmall.bean.ProductImage;
import tmall.bean.PropertyValue;
import tmall.bean.Review;
import tmall.bean.User;
import tmall.comparator.ProductAllComparator;
import tmall.comparator.ProductDateComparator;
import tmall.comparator.ProductPriceComparator;
import tmall.comparator.ProductReviewComparator;
import tmall.comparator.ProductSaleCountComparator;
import tmall.dao.CategoryDAO;
import tmall.dao.OrderDAO;
import tmall.dao.ProductDAO;
import tmall.dao.ProductImageDAO;
import tmall.util.Page;
/**
 * 首页需要什么数据呢？从首页展示需求是哪个来看：
 * 1.在横向导航栏上提供4个分类连接
 * 2.在纵向导航栏上提供全部17个分类连接
 * 3.当鼠标移动到某一个纵向分类的连接的时候，显示这个分类下的推荐商品
 * 4.按照每种分类显示5个商品的方式，显示17种分类
 * @author hzx
 *
 */
public class ForeServlet extends BaseForeServlet {
	public String home(HttpServletRequest request, HttpServletResponse response, Page page) {
		List<Category> cs= new CategoryDAO().list(); //获取所有的17种分类
		new ProductDAO().fill(cs);//为这些分类填充产品集合，即为每个Category对象，设置products属性
		new ProductDAO().fillByRow(cs);//为这些分类填充推荐产品集合，即为每个Category对象，设置productsByRow属性
		request.setAttribute("cs", cs);//把分类集合设置在request的"cs"属性上
		return "home.jsp";//服务器跳转到home.jsp
	}
	
	/**
	 * 1.获取账号密码
	 * 2.通过HtmlUtils.htmlEscape(name)；把账号里的特殊符号进行转义
	 * 3.判断用户名是否存在
	 * 	 3.1如何已经存在，就服务端跳转到register.jsp，并且带错误提示信息
	 * 	 3.2如果不存在，就加到数据库中，并数据库跳转到registerSuccess.jsp页面
	 * 
	 * 注为什么要用 HtmlUtils.htmlEscape？ 因为有些同学在恶意注册的时候，会使用诸如 <script>alert('papapa')</script> 
	 * 这样的名称，会导致网页打开就弹出一个对话框。 那么在转义之后，就没有这个问题了
	 * @param request
	 * @param response
	 * @param page
	 * @return
	 */
	public String register(HttpServletRequest request, HttpServletResponse response, Page page) {
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        name = HtmlUtils.htmlEscape(name);
        System.out.println(name);
        boolean exist = userDAO.isExist(name);
         
        if(exist){
            request.setAttribute("msg", "用户名已经被使用,不能使用");
            return "register.jsp"; 
        }
         
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        System.out.println(user.getName());
        System.out.println(user.getPassword());
        userDAO.add(user);
         
        return "@registerSuccess.jsp"; 
    }  
	
	/**loginPage.jsp的form提交数据到forelogin，导致ForeServlet.login()方法被调用
	 * 1.获取账号密码
	 * 2.把账号通过HtmlUtils.htmlEscape进行转义
	 * 3.根据账号和密码获取User对象
	 * 	3.1如果对象为空，则服务端跳转回login.jsp，也带上错误信息，并且使用login.jsp中的方法显示错误信息
	 * 	3.2如果对象存在，则把对象保存在session中，并客户端跳转到首页"@forehome"
	 * @param request
	 * @param response
	 * @param page
	 * @return
	 */
	 public String login(HttpServletRequest request, HttpServletResponse response, Page page) {
	        String name = request.getParameter("name");
	        name = HtmlUtils.htmlEscape(name);
	        String password = request.getParameter("password");    
	         
	        User user = userDAO.get(name,password);
	          
	        if(null==user){
	            request.setAttribute("msg", "账号密码错误");
	            return "login.jsp";
	        }
	        request.getSession().setAttribute("user", user);
	        return "@forehome";
	    }  
	 
	 /**
	  * 1.获取参数pid
	  * 2.根据pid获取Product对象p
	  * 3.根据对象p，获取这个产品对应的单个图片集合
	  * 4.根据对象p，获取这个产品对应的详情图片集合
	  * 5.获取产品的所有属性值
	  * 6.获取产品对应的所有的评价
	  * 7.设置产品的销量和评价数量
	  * 8.把上述取值放在request属性上
	  * 9.服务端跳转到"product.jsp"页面
	  * @param request
	  * @param response
	  * @param page
	  * @return
	  */
	 public String product(HttpServletRequest request, HttpServletResponse response, Page page) {
	        int pid = Integer.parseInt(request.getParameter("pid"));
	        Product p = productDAO.get(pid);
	         
	        List<ProductImage> productSingleImages = productImageDAO.list(p, ProductImageDAO.type_single);
	        List<ProductImage> productDetailImages = productImageDAO.list(p, ProductImageDAO.type_detail);
	        p.setProductSingleImages(productSingleImages);
	        p.setProductDetailImages(productDetailImages);
	         
	        List<PropertyValue> pvs = propertyValueDAO.list(p.getId());      
	     
	        List<Review> reviews = reviewDAO.list(p.getId());
	         
	        productDAO.setSaleAndReviewNumber(p);
	 
	        request.setAttribute("reviews", reviews);
	 
	        request.setAttribute("p", p);
	        request.setAttribute("pvs", pvs);
	        return "product.jsp";      
	    }
	 
	 /**
	  * 1.在session中去掉“user”
	  * 2.客户端跳转到首页
	  * 
	  * @param request
	  * @param response
	  * @param page
	  * @return
	  */
	    public String logout(HttpServletRequest request, HttpServletResponse response, Page page) {
	        request.getSession().removeAttribute("user");
	        return "@forehome";
	    }  
	    
	    
	   /**
	    * 在上一步ajax访问路径 /forecheckLogin会导致ForeServlet.checkLogin()方法被调用
	    * 获取session中的"user"对象
	    *  	如果不为空，表示已登录，返回字符串"success"
	    *   如果为空，表示未登录，返回字符串"fail" 
	    * @param request
	    * @param response
	    * @param page
	    * @return
	    */
	   public String checkLogin(HttpServletRequest request,HttpServletResponse response, Page page){
		   User user = (User) request.getSession().getAttribute("user");
		   if(null!=user) return "%success";
		   return "%fail";
	   }
	   
	   /**
	    * 在上一步modal.jsp中，点击了登录按钮之后，访问路径/foreloginAjax，导致ForeServlet.loginAjax()方法被调用
	    * 1.获取账号密码
	    * 2.通过账号密码获取User对象
	    * 	2.1如果User对象为空，那么就返回“fail”字符串
	    * 	2.2如果User对象不为空，那么就把User对象放在session中，并且返回“success”字符串
	    * @param request
	    * @param response
	    * @param page
	    * @return
	    */
	   public String loginAjax(HttpServletRequest request, HttpServletResponse response, Page page) {
	        String name = request.getParameter("name");
	        String password = request.getParameter("password");    
	        User user = userDAO.get(name,password);
	         
	        if(null==user){
	            request.setAttribute("msg", "账号密码错误");
	            return "%fail";
	        }
	        request.getSession().setAttribute("user", user);
	        return "%success"; 
	    } 
	   
	   /**
	    * 1.获取参数cid
	    * 2.根据cid获取分类Category对象c
	    * 3.为c填充产品
	    * 4.为产品填充销量和评价数据
	    * 5.获取参数sort
	    * 	5.1如果sort==null，即不排序
	    * 	5.2如果sort!=null，则根据sort值，从5个comparator比较其中选择一个对应的排序器进行排序
	    * 6.把c放在request中
	    * 7.服务端跳转到category.jsp中
	    * @param request
	    * @param response
	    * @param page
	    * @return
	    */
	   public String category(HttpServletRequest request, HttpServletResponse response, Page page) {
		    int cid = Integer.parseInt(request.getParameter("cid"));
		     
		    Category c = new CategoryDAO().get(cid);
		    new ProductDAO().fill(c);
		    new ProductDAO().setSaleAndReviewNumber(c.getProducts());      
		     
		    String sort = request.getParameter("sort");
		    if(null!=sort){
		    switch(sort){
		        case "review":
		            Collections.sort(c.getProducts(),new ProductReviewComparator());
		            break;
		        case "date" :
		            Collections.sort(c.getProducts(),new ProductDateComparator());
		            break;
		             
		        case "saleCount" :
		            Collections.sort(c.getProducts(),new ProductSaleCountComparator());
		            break;
		             
		        case "price":
		            Collections.sort(c.getProducts(),new ProductPriceComparator());
		            break;
		             
		        case "all":
		            Collections.sort(c.getProducts(),new ProductAllComparator());
		            break;
		        }
		    }
		     
		    request.setAttribute("c", c);
		    return "category.jsp";     
		}
	   
	   /**
	    * 通过search.jsp或者simpleSearch.jsp 提交数据到路径/foresearch,导致ForeServlet.search()方法被调用
	    * 1.获取参数keyword
	    * 2.根据keyword进行模糊查询，获取满足条件的前20个商品
	    * 3.为这些产品设置销量和评价数量
	    * 4.把产品结合设置在request的"ps"属性上
	    * 5.服务端跳转到serachResult.jsp页面
	    * @param request
	    * @param response
	    * @param page
	    * @return
	    */
	   public String search(HttpServletRequest request, HttpServletResponse response, Page page){
		    String keyword = request.getParameter("keyword");
		    List<Product> ps= new ProductDAO().search(keyword,0,20);
		    productDAO.setSaleAndReviewNumber(ps);
		    request.setAttribute("ps",ps);
		    return "searchResult.jsp";
		} 
	   
	   /**
	    * 通过上个步骤访问的地址 /forebuyone 导致ForeServlet.buyone()方法被调用
	    * 1.获取参数pid
	    * 2.获取参数num
	    * 3.根据pid获取产品对象p
	    * 4.从session中获取用户对象user
	    * 
	    * 就下来就是新增订单项OrderItem，新增订单项要考虑两个情况
	    * a.如果已经存在这个产品对应的OrderItem，并且还没生成订单，即还在购物车中。那么就应该在对应的OrderItem基础上，调整数量
	    *  a.1基于用户对象user，查询没有生成订单的订单项集合
	    *  a.2遍历这个集合
	    *  a.3如果产品是一样的话，进行数量追加
	    *  a.4获取这个订单项的id
	    * @param request
	    * @param response
	    * @param page
	    * @return
	    */
	   public String buyone(HttpServletRequest request, HttpServletResponse response, Page page) {
		    int pid = Integer.parseInt(request.getParameter("pid"));
		    int num = Integer.parseInt(request.getParameter("num"));
		    Product p = productDAO.get(pid);
		    int oiid = 0;
		     
		    User user =(User) request.getSession().getAttribute("user");
		    boolean found = false;
		    List<OrderItem> ois = orderItemDAO.listByUser(user.getId());
		    for (OrderItem oi : ois) {
		        if(oi.getProduct().getId()==p.getId()){
		            oi.setNumber(oi.getNumber()+num);
		            orderItemDAO.update(oi);
		            found = true;
		            oiid = oi.getId();
		            break;
		        }
		    }      
		 
		    if(!found){
		        OrderItem oi = new OrderItem();
		        oi.setUser(user);
		        oi.setNumber(num);
		        oi.setProduct(p);
		        orderItemDAO.add(oi);
		        oiid = oi.getId();
		    }
		    return "@forebuy?oiid="+oiid;
		}
	   
	   /**
	    * 在立即购买的最后，客户端跳转到结算路径 "@forebuy?oiid="+oiid"导致ForeServlet.buy()方法被调用
	    * 1.通过getParameterValues获取参数ooid
	    * 	为什么这里要用getParameterValues试图获取多个ooid，而不是getParameter仅仅获取一个ooid？因为根据购物流程环节与表关系，结算页面还需要
	    * 	显示在购物车中选中多条OrderItem数据，所以为了兼容从购物车页面跳转过来的需求，要用getParameterValues获取多个ooid
	    * 2.准备一个泛型是OrderItem的结合ois
	    * 3.根据前面步骤获取的ooids，从数据库取出OrderItem对象，并且放入ois集合中
	    * 4.累计这些ois的价格综述，赋值在total上
	    * 5.把订单项集合放在sesseion的属性“ois”上
	    * 6.把总价格放在request的属性“total”上
	    * 7.服务端跳转到buy.jsp
	    * 
	    * @param request
	    * @param response
	    * @param page
	    * @return
	    */
	   public String buy(HttpServletRequest request, HttpServletResponse response, Page page){
		    String[] oiids=request.getParameterValues("oiid");
		    List<OrderItem> ois = new ArrayList<>();
		    float total = 0;
		 
		    for (String strid : oiids) {
		        int oiid = Integer.parseInt(strid);
		        OrderItem oi= orderItemDAO.get(oiid);
		        total +=oi.getProduct().getPromotePrice()*oi.getNumber();
		        ois.add(oi);
		    }
		     
		    request.getSession().setAttribute("ois", ois);
		    request.setAttribute("total", total);
		    return "buy.jsp";
		} 
	   /**
	    * 在访问/foreaddCart导致ForeServlet.addCart()方法被调用
	    * addCart()方法和立即购买中的ForeServlet.buyone()步骤做的事情是一样的，区别在于返回的不同
	    * 1.获取参数pid
	    * 2.获取参数num
	    * 3.根据pid获取产品对象p
	    * 4.从sesseion中获取用户对象user
	    * 
	    * 接下来就是新增订单项OrderItem，新增订单要考虑两个情况
	    * a. 如果已经存在这个产品对应的OrderItem，并且还没有生成订单，即还在购物车中。 那么就应该在对应的OrderItem基础上，调整数量
			a.1 基于用户对象user，查询没有生成订单的订单项集合
			a.2 遍历这个集合
			a.3 如果产品是一样的话，就进行数量追加
			a.4 获取这个订单项的 id

			b. 如果不存在对应的OrderItem,那么就新增一个订单项OrderItem
			b.1 生成新的订单项
			b.2 设置数量，用户和产品
			b.3 插入到数据库
			b.4 获取这个订单项的 id
			与ForeServlet.buyone()客户端跳转到结算页面不同的是，最后返回的字符串是"success",表示添加成功
	    * 
	    * @param request
	    * @param response
	    * @param page
	    * @return
	    */
	   public String addCart(HttpServletRequest request, HttpServletResponse response, Page page) {
	        int pid = Integer.parseInt(request.getParameter("pid"));
	        Product p = productDAO.get(pid);
	        int num = Integer.parseInt(request.getParameter("num"));
	         
	        User user =(User) request.getSession().getAttribute("user");
	        boolean found = false;
	 
	        List<OrderItem> ois = orderItemDAO.listByUser(user.getId());
	        for (OrderItem oi : ois) {
	            if(oi.getProduct().getId()==p.getId()){
	                oi.setNumber(oi.getNumber()+num);
	                orderItemDAO.update(oi);
	                found = true;
	                break;
	            }
	        }      
	         
	        if(!found){
	            OrderItem oi = new OrderItem();
	            oi.setUser(user);
	            oi.setNumber(num);
	            oi.setProduct(p);
	            orderItemDAO.add(oi);
	        }
	        return "%success";
	    }
	   
	   /**
	    * 访问地址/forecart导致ForeServlet.cart()方法被调用
	    * 1.通过session获取当前用户 所以一定要登录才访问，否则拿不到用户对象
	    * 2.获取被这个用户关联的订单项集合 ois
	    * 3.把ois放在request中
	    * 4.服务端跳转到cart.jsp
	    * @param request
	    * @param response
	    * @param page
	    * @return
	    */
	   public String cart(HttpServletRequest request, HttpServletResponse response, Page page) {
		    User user =(User) request.getSession().getAttribute("user");
		    List<OrderItem> ois = orderItemDAO.listByUser(user.getId());
		    request.setAttribute("ois", ois);
		    return "cart.jsp";
		}  
	   
	   /**
	    * 点击增加或者减少按键后，根据cartPage.jsp中的js代码，会通过Ajax访问/forechangeOrderItem路径，导致
	    * ForeServlet.changeOrderItem()方法被调用
	    * 1.判断用户是否登录
	    * 2.获取pid和number
	    * 3.遍历出用户当前所有的未生成订单的OrderItem
	    * 4.根据pid找到匹配的OrderItem，并修改数量后更新到数据库
	    * 5.返回字符串“success”
	    * @param request
	    * @param response
	    * @param page
	    * @return
	    */
	   public String changeOrderItem(HttpServletRequest request, HttpServletResponse response, Page page) {
	        User user =(User) request.getSession().getAttribute("user");
	        if(null==user)
	            return "%fail";
	 
	        int pid = Integer.parseInt(request.getParameter("pid"));
	        int number = Integer.parseInt(request.getParameter("number"));
	        List<OrderItem> ois = orderItemDAO.listByUser(user.getId());
	        for (OrderItem oi : ois) {
	            if(oi.getProduct().getId()==pid){
	                oi.setNumber(number);
	                orderItemDAO.update(oi);
	                break;
	            }
	             
	        }      
	        return "%success";
	    }
	   
	   /**点击删除按钮后，根据cartPage.jsp中的js代码，会通过ajax访问/foredelteOrderItem路径，导致ForeServlet.deleteOrderItem方法被调用
	    * 1.判断用户是否登录 （其实这里不需要了吧 因为有filer）
	    * 2.获取ooid
	    * 3.删除ooid对应的OrderItem数据
	    * 4.返回字符串success
	    * @param request
	    * @param response
	    * @param page
	    * @return
	    */
	   public String deleteOrderItem(HttpServletRequest request, HttpServletResponse response, Page page){
		    User user =(User) request.getSession().getAttribute("user");
		    if(null==user)
		        return "%fail";
		    int oiid = Integer.parseInt(request.getParameter("oiid"));
		    orderItemDAO.delete(oiid);
		    return "%success";
		}
	   
	   /**
	    * 提交订单访问路径/forecreateOrder,导致ForeServlet.createOrder方法被调用
	    * 1.从session中获取user对象
	    * 2.获取地址，邮编，收货人，用户留言等信息
	    * 3.根据当前时间加上一个4位随机数生成订单
	    * 4.根据上述参数，创建订单对象
	    * 5.把订单状态设置为等待支付
	    * 6.加入到数据库有
	    * 7.从session中获取订单项的集合（在结算功能的ForeServlet.buy()13行，订单项集合被放到了session中）
	    * 8.遍历订单项集合，设置每个订单项的order，并更新到数据库
	    * 9.统计本次订单的总金额
	    * 10.客户端跳转到确认支付页的forealipay,并且带上订单的id和总金额
	    * @param request
	    * @param response
	    * @param page
	    * @return
	    */
	   public String createOrder(HttpServletRequest request, HttpServletResponse response, Page page){
		    User user =(User) request.getSession().getAttribute("user");
		     
		    List<OrderItem> ois= (List<OrderItem>) request.getSession().getAttribute("ois");
		    if(ois==null)//之前站长的isEmpty实际上是错误的
		        return "@login.jsp";
		 
		    String address = request.getParameter("address");
		    String post = request.getParameter("post");
		    String receiver = request.getParameter("receiver");
		    String mobile = request.getParameter("mobile");
		    String userMessage = request.getParameter("userMessage");
		     
		    Order order = new Order();
		    String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) +RandomUtils.nextInt(10000);
		 
		    order.setOrderCode(orderCode);
		    order.setAddress(address);
		    order.setPost(post);
		    order.setReceiver(receiver);
		    order.setMobile(mobile);
		    order.setUserMessage(userMessage);
		    order.setCreateDate(new Date());
		    order.setUser(user);
		    order.setStatus(OrderDAO.waitPay);
		 
		    orderDAO.add(order);
		    float total =0;
		    for (OrderItem oi: ois) {
		        oi.setOrder(order);
		        orderItemDAO.update(oi);
		        total+=oi.getProduct().getPromotePrice()*oi.getNumber();
		    }
		     
		    return "@forealipay?oid="+order.getId() +"&total="+total;
		}
	   
	   public String alipay(HttpServletRequest request, HttpServletResponse response, Page page){
	        return "alipay.jsp";
	    }
	 
	   /**
	    * 1.在上一步确认访问按钮提交数据到/forepayed，导致ForeServlet.payed方法被调用
	    * 	1.1获取参数oid
	    * 	1.2根据oid获取到订单对象order
	    * 	1.3修改订单的状态和支付时间
	    * 	1.4更新这个订单对象到数据库
	    * 	1.5把这个订单放在request的属性"o"上
	    * 	1.6服务端跳转到payed.jsp
	    * 2.payed.jsp
	    * 	与register.jsp相仿，payed.jsp也包含了，header.jsp，top.jsp,simpleSearch.jsp，foot
	    * @param request
	    * @param response
	    * @param page
	    * @return
	    */
	    public String payed(HttpServletRequest request, HttpServletResponse response, Page page) {
	        int oid = Integer.parseInt(request.getParameter("oid"));
	        Order order = orderDAO.get(oid);
	        order.setStatus(OrderDAO.waitDelivery);
	        order.setPayDate(new Date());
	        new OrderDAO().update(order);
	        request.setAttribute("o", order);
	        return "payed.jsp";    
	    }  
	    
	    /**
	     *	/forebought 导致ForeServlet.bought()方法被调用
	     *	1.通过sesseion获取用户user
	     *	2.查询user所有状态不是“delete”的订单集合os
	     *	3.为这些订单填充订单项
	     *	4.把os放在request的属性“os”上
	     *	5.服务端跳转到bought.jsp上
	     * @param request
	     * @param response
	     * @param page
	     * @return
	     */
	    public String bought(HttpServletRequest request, HttpServletResponse response, Page page) {
	        User user =(User) request.getSession().getAttribute("user");
	        List<Order> os= orderDAO.list(user.getId(),OrderDAO.delete);
	         
	        orderItemDAO.fill(os);
	         
	        request.setAttribute("os", os);
	         
	        return "bought.jsp";       
	    }
	    
	    /**
	     * 1.点击确认收款后，访问地址/foreconfirmPay
	     * 2.ForeServlet.confirmPay()方法被调用
	     * 	2.1 获取参数oid
	     * 	2.2 通过oid获取订单对象o
	     * 	2.3 为订单对象填充订单项
	     * 	2.4 把订单对象放在requst的属性"o"上
	     * 	2.5 服务器跳转到confirmPay.jsp上
	     * 3.confirmPay.jsp
	     * 		与 register.jsp 相仿，confirmPay.jsp也包括了header.jsp, top.jsp, simpleSearch.jsp, footer.jsp等公共页面
	     * 	中间是订单确认业务页面 confirmPayPage.jsp
	     * 4.confirmPayPage.jsp
	     * 	显示订单的创建创建时间，付款时间和发货时间，以及订单号，收款人信息等
	     * 	遍历订单项集合，显示其中的产品图片，产品标题，价格，数量，小计，总结信息
	     * 最后提供确认支付按钮，提交到/foreorderconfirmed路径
	     * @param request
	     * @param response
	     * @param page
	     * @return
	     */
	    public String confirmPay(HttpServletRequest request, HttpServletResponse response, Page page) {
	        int oid = Integer.parseInt(request.getParameter("oid"));
	        Order o = orderDAO.get(oid);
	        orderItemDAO.fill(o);
	        request.setAttribute("o", o);
	        return "confirmPay.jsp";       
	    }
	    
	    
	    /**
	     * 通过上一步最后的确认支付按钮，提交到路径/foreorderConfirmed，导致ForeServlet.orderConfirmed()方法被调用
			1. ForeServlet.orderConfirmed() 方法
				1.1 获取参数oid
				1.2 根据参数oid获取Order对象o
				1.3 修改对象o的状态为等待评价，修改其确认支付时间
				1.4 更新到数据库
				1.5 服务端跳转到orderConfirmed.jsp页面
			2. orderConfirmed.jsp
				与 register.jsp 相仿，orderConfirmed.jsp也包含了header.jsp, top.jsp, simpleSearch.jsp， footer.jsp 等公共页面。
			中间是确认收货成功业务页面 orderConfirmedPage.jsp
			3. orderConfirmedPage.jsp
				显示"交易已经成功，卖家将收到您的货款。"
	     * @param request
	     * @param response
	     * @param page
	     * @return
	     */
	    public String orderConfirmed(HttpServletRequest request, HttpServletResponse response, Page page) {
	        int oid = Integer.parseInt(request.getParameter("oid"));
	        Order o = orderDAO.get(oid);
	        o.setStatus(OrderDAO.waitReview);
	        o.setConfirmDate(new Date());
	        orderDAO.update(o);
	        return "orderConfirmed.jsp";
	    }
	   
	    /**
	     * 在我的订单页点击删除按钮，根据boughtPage.jsp中的ajax操作，会访问路径/foredelteOrder,导致ForeServlet.deleteOrder方法被调用
	     * 	1.ForeServlet.deleteOrder()
	     * 		1.1获取参数oid
	     * 		1.2根据oid获取订单对象o
	     * 		1.3修改状态
	     * 		1.4更新到数据库
	     * 		1.5返回字符串success
	     * 2.boughtPage.jsp的javascript代码获取返回字符串是success的时候，隐藏掉当前这行订单数据
	     * @param request
	     * @param response
	     * @param page
	     * @return
	     */
	    public String deleteOrder(HttpServletRequest request, HttpServletResponse response, Page page){
	        int oid = Integer.parseInt(request.getParameter("oid"));
	        Order o = orderDAO.get(oid);
	        o.setStatus(OrderDAO.delete);
	        orderDAO.update(o);
	        return "%success";     
	    }
	    
	    /**
	     * 通过点击评价按钮，来到路径/forereview,导致ForeServlet.reveiw()方法被调用
	     * 1. ForeServlet.review()
	     * 	1.1获取参数oid		
	     * 	1.2根据oid获取订单对象o
	     * 	1.3为订单对象填充订单项
	     * 	1.4获取第一个订单项对应的产品，因为在评价页面需要显示一个产品图片，那么久使用第一个产品的图片了
	     * 	1.5获取这个产品的评价的集合
	     * 	1.6为产品设置评价数量和销量
	     * 	1.7把产品、订单和评价集合放在request上
	     * 	1.8服务端跳转到rewview.jsp
	     * 
	     * 2.review.jsp
	     * 	与register.jsp相仿，review.jsp也包含了heaer.jsp,top.jsp,simpleSearch.jsp,footer.jsp等公共页面
	     * 中间是产品业务页面reviewPage.jsp
	     * 3.reviewPage.jsp
	     * 	在reviewPage.jsp中显示产品图片，产品标题，价格，产品销量，产品评价数量，以及订单信息等等。
	     * 	同时还显示出了该产品所有的评价，但是默认是隐藏
	     * 
	     * @param request
	     * @param response
	     * @param page
	     * @return
	     */
	    public String review(HttpServletRequest request, HttpServletResponse response, Page page) {
			int oid = Integer.parseInt(request.getParameter("oid"));
			Order o = orderDAO.get(oid);
			orderItemDAO.fill(o);
			Product p = o.getOrderItems().get(0).getProduct();
			List<Review> reviews = reviewDAO.list(p.getId());
			productDAO.setSaleAndReviewNumber(p);
			request.setAttribute("p", p);
			request.setAttribute("o", o);
			request.setAttribute("reviews", reviews);
			return "review.jsp";		
		}
	    
	    /**
	     * 在评价产品页面点击提交评价，就把数据提交到了/foredoreview路径，导致ForeServlet.doreview方法被调用
	     * 1. ForeServlet.doreview()
			1.1 获取参数oid
			1.2 根据oid获取订单对象o
			1.3 修改订单对象状态
			1.4 更新订单对象到数据库
			1.5 获取参数pid
			1.6 根据pid获取产品对象
			1.7 获取参数content (评价信息)
			1.8 对评价信息进行转义，道理同注册ForeServlet.register()
			1.9 从session中获取当前用户
			1.10 创建评价对象review
			1.11 为评价对象review设置 评价信息，产品，时间，用户
			1.12增加到数据库
			1.13.客户端跳转到/forereview： 评价产品页面，并带上参数showonly=true
			2. reviewPage.jsp
			在re	viewPage.jsp中，当参数showonly==true，那么就显示当前产品的所有评价信息
	     * @param request
	     * @param response
	     * @param page
	     * @return
	     */
	    public String doreview(HttpServletRequest request, HttpServletResponse response, Page page) {
			int oid = Integer.parseInt(request.getParameter("oid"));
			Order o = orderDAO.get(oid);
			o.setStatus(OrderDAO.finish);
			orderDAO.update(o);
			int pid = Integer.parseInt(request.getParameter("pid"));
			Product p = productDAO.get(pid);
			
			String content = request.getParameter("content");
			
			content = HtmlUtils.htmlEscape(content);

			User user =(User) request.getSession().getAttribute("user");
			Review review = new Review();
			review.setContent(content);
			review.setProduct(p);
			review.setCreateDate(new Date());
			review.setUser(user);
			reviewDAO.add(review);
			
			return "@forereview?oid="+oid+"&showonly=true";		
		}
}
