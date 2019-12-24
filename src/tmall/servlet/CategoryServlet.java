package tmall.servlet;


import java.awt.image.BufferedImage;	
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tmall.bean.Category;
import tmall.util.ImageUtil;
import tmall.util.Page;

public class CategoryServlet extends BaseBackServlet {
	
	/**
	 * 1.parseUpload获取上传文件的输入流
	 * 2.parseUpload方法会修改params参数，并且把浏览器提交的name信息放在其中
	 * 3.从params中取出name信息，并根据这个name信息，借助categoryDAO，向数据库中插入数据
	 * 4.根据.getServletContext().getRealPath("img/category")，定位到存放分类图片的目录
	 * 5.文件命名以保存到数据库的分类对象的+".jpg"的格式命名
	 * 6.根据步骤1获取的输入流，把浏览器提交的文件，复制到目标文件
	 * 7.借助IUtil.change2jpg()方法把格式真正转化为jpg，而不仅仅是后缀名为.jpg
	 */
	public String add(HttpServletRequest request, HttpServletResponse response, Page page) {
		Map<String,String> params = new HashMap<>();
		InputStream is = super.parseUpload(request, params);
		
		String name= params.get("name");
		Category c = new Category();
		c.setName(name);
		categoryDAO.add(c);
		
		File  imageFolder= new File(request.getSession().getServletContext().getRealPath("img/category"));
		File file = new File(imageFolder,c.getId()+".jpg");
		
		try {
			if(null!=is && 0!=is.available()){
			    try(FileOutputStream fos = new FileOutputStream(file)){
			        byte b[] = new byte[1024 * 1024];
			        int length = 0;
			        while (-1 != (length = is.read(b))) {
			            fos.write(b, 0, length);
			        }
			        fos.flush();
			        //通过如下代码，把文件保存为jpg格式
			        BufferedImage img = ImageUtil.change2jpg(file);
			        ImageIO.write(img, "jpg", file);		
			    }
			    catch(Exception e){
			    	e.printStackTrace();
			    }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return "@admin_category_list";
	}

	
	public String delete(HttpServletRequest request, HttpServletResponse response, Page page) {
		int id = Integer.parseInt(request.getParameter("id"));
		categoryDAO.delete(id);
		return "@admin_category_list";
	}

	
	public String edit(HttpServletRequest request, HttpServletResponse response, Page page) {
		int id = Integer.parseInt(request.getParameter("id"));
		Category c = categoryDAO.get(id);
		request.setAttribute("c", c);
		return "admin/editCategory.jsp";		
	}

	/**
	 * update方法和add方法处理很类似，有所不同在于增加操作一定会提交图片，而修改不一定提交图片
	 * 在update中做如下操作
	 * 1.parseUpload获取上传文件的输入流
	 * 2.parseUpload方法会修改params参数，并且把浏览器提交的name信息放在其中
	 * 3.从params中取出id和name信息，并且根据这个id，name信息创建新的对象，并且借助categoryDAO，向数据库更新信息
	 * 4.根据request.getServletContext().getRealPath("img/category"),定位到存放分类图片的目录
	 * 5.文件命令以保存到数据库的分类对象的+".jpg"格式命名
	 * 6.如果通过parseLoad获取到的输入流是空的，或者其中的可取字节数为0，那么久不进行上传。
	 * 7.根据步骤1获取的输入流，把浏览器提交的文件，复制到目标文件
	 * 8.借助Imageutil.change2jp2()方法把格式真正转化为jpg，而不仅仅是后缀名为.jpg
	 * 9.最后客户端跳转到admin_category_list
	 */
	public String update(HttpServletRequest request, HttpServletResponse response, Page page) {
		Map<String,String> params = new HashMap<>();
		InputStream is = super.parseUpload(request, params);
		
		System.out.println(params);//测试代码
		String name= params.get("name");
		int id = Integer.parseInt(params.get("id"));

		Category c = new Category();
		c.setId(id);
		c.setName(name);
		categoryDAO.update(c);
		
		File  imageFolder= new File(request.getSession().getServletContext().getRealPath("img/category"));
		File file = new File(imageFolder,c.getId()+".jpg");
		file.getParentFile().mkdirs();
		
		try {
			if(null!=is && 0!=is.available()){
			    try(FileOutputStream fos = new FileOutputStream(file)){
			        byte b[] = new byte[1024 * 1024];
			        int length = 0;
			        while (-1 != (length = is.read(b))) {
			            fos.write(b, 0, length);
			        }
			        fos.flush();
			        //通过如下代码，把文件保存为jpg格式
			        BufferedImage img = ImageUtil.change2jpg(file);
			        ImageIO.write(img, "jpg", file);		
			    }
			    catch(Exception e){
			    	e.printStackTrace();
			    }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "@admin_category_list";

	}

	public String list(HttpServletRequest request, HttpServletResponse response, Page page) {
		List<Category> cs = categoryDAO.list(page.getStart(),page.getCount());
		int total = categoryDAO.getTotal();
		page.setTotal(total);
		request.setAttribute("thecs", cs);
		request.setAttribute("page", page);
		return "admin/listCategory.jsp";
	}
}
