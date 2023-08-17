package Servlet ;
/*
 * 待完成：用MVC模式分开DB和Action操作
 * 增删改查看导印统功能的实现
 */

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONException;
import org.json.JSONObject;
import project.dao.Data;
import project.dao.DeviceDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
public class ServletAction extends HttpServlet {
		String sub = "file";

	public void showDebug(String msg){
		System.out.println("["+(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date())+"]["+sub+"/ServletAction]"+msg);
	}
	/*
	 * 处理顺序：先是service，后根据情况doGet或者doPost
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		processAction(request,response);
	}
	/*========================================函数分流 开始========================================*/
	public void processAction(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		HttpSession session = request.getSession();
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		boolean actionOk = false;
		int resultCode=0;
		String resultMsg="ok";
		JSONObject json=new JSONObject();
		showDebug("processAction收到的action是："+action);
		if (action == null){
			resultMsg="传递过来的action是NULL";
		}else{
			//这几个常规增删改查功能
			if (action.equals("get_device_record")) {
				actionOk=true;
				try {
					getDeviceRecord(request, response, json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (action.equals("add_device_record")) {
				actionOk=true;
				try {
					addDeviceRecord(request, response, json);
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (action.equals("modify_device_record")) {
				actionOk=true;
				try {
					modifyDeviceRecord(request, response, json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (action.equals("delete_device_record")) {
				actionOk=true;
				try {
					deleteDeviceRecord(request, response, json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (action.equals("upload_device_record")) {
				actionOk=true;
				try {
					uploadDeviceRecord(request, response, json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				responseBack(request,response,json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	/*========================================函数分流 结束========================================*/
	/*========================================公共函数 开始========================================*/
	private Data getPageParameters(HttpServletRequest request, HttpServletResponse response, JSONObject json) throws JSONException{
		Data data=new Data();
		HttpSession session = request.getSession();
		/*----------------------------------------获取所有表单信息 开始----------------------------------------*/
		showDebug("[getPageParameters]----------------------------------------获取所有表单信息 开始----------------------------------------");
		JSONObject param=data.getParam();
		Enumeration requestNames=request.getParameterNames();
		for(Enumeration e=requestNames;e.hasMoreElements();){
			String thisName=e.nextElement().toString();
			String thisValue=request.getParameter(thisName);
			showDebug("[getPageParameters]"+thisName+"="+thisValue);
			param.put(thisName, thisValue);
		}
		showDebug("[getPageParameters]data的Param="+data.getParam().toString());
		showDebug("[getPageParameters]----------------------------------------获取所有表单信息 完毕----------------------------------------");
		/*----------------------------------------获取所有表单信息 完毕----------------------------------------*/
		return data;
	}
	private void responseBack(HttpServletRequest request,HttpServletResponse response,JSONObject json) throws JSONException {
		boolean isAjax=true;if (request.getHeader("x-requested-with") == null || request.getHeader("x-requested-with").equals("com.tencent.mm")){isAjax=false;}	//判断是异步请求还是同步请求，腾讯的特殊
		showDebug("[responseBack]收到的信息头："+request.getHeader("x-requested-with"));
		if(isAjax){
			response.setContentType("application/json; charset=UTF-8");
			try {
				response.getWriter().print(json);
				response.getWriter().flush();
				response.getWriter().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			String action=json.getString("action");
			String errorNo="0";
			String errorMsg="ok";
			String url = "device_list.jsp?action="+action+"&result_code="+errorNo+ "&result_msg=" + errorMsg;
			if(json.has("redirect_url")) url=json.getString("redirect_url");
			try {
				response.sendRedirect(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/*========================================公共函数 结束========================================*/
	/*========================================CRUD业务函数 开始========================================*/
	private void getDeviceRecord(HttpServletRequest request, HttpServletResponse response,JSONObject json) throws JSONException, SQLException {
		DeviceDao dao=new DeviceDao();
		Data data=getPageParameters(request,response,json);
		dao.getDeviceRecord(data,json);
	}
	private void modifyDeviceRecord(HttpServletRequest request, HttpServletResponse response,JSONObject json) throws JSONException, SQLException {
		DeviceDao dao=new DeviceDao();
		Data data=getPageParameters(request,response,json);
		dao.modifyDeviceRecord(data,json);
	}
	private void deleteDeviceRecord(HttpServletRequest request, HttpServletResponse response,JSONObject json) throws JSONException, SQLException {
		DeviceDao dao=new DeviceDao();
		Data data=getPageParameters(request,response,json);
		dao.deleteDeviceRecord(data,json);
	}
	private void addDeviceRecord(HttpServletRequest request, HttpServletResponse response,JSONObject json) throws JSONException, SQLException {
		DeviceDao dao=new DeviceDao();
		Data data=getPageParameters(request,response,json);
		dao.addDeviceRecord(data,json);
	}
	/*========================================CRUD业务函数 结束========================================*/
	private void uploadDeviceRecord(HttpServletRequest request, HttpServletResponse response,JSONObject json) throws JSONException, SQLException, UnsupportedEncodingException {
		Data data=getPageParameters(request,response,json);
		uploadRecord(request,data,json);
		DeviceDao dao=new DeviceDao();
		dao.saveUploadRecord(data,json);
	}

	/*
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
注意是用fuleupload的类，不是Tomcat的
	*/
	private void uploadRecord(HttpServletRequest request,Data data, JSONObject json) throws UnsupportedEncodingException, JSONException {
		String resultMsg="ok";
		int resultCode=0;
		String title=null;
		String limitTime=null;
		/*String rootPath = this.getServletContext().getRealPath("/");//获取当前文件的绝对路径*/
		String path =  "C:";
		String savePath = "/";
		String downloadUrl="";                              //传到前端的下载链接
		List jsonFile=new ArrayList();
		HashMap<String, String> extMap = new HashMap<String, String>();
		extMap.put("upload", "doc,docx,pdf,txt,xml,xls,xlsx,xml,ppt,pptx,jpg,png,jfjf,gif");//设置上传文件到文件夹file下，文件类型只能为doc docx...这几类
		long maxSize = 1000000000;//设置上传的文件大小最大为1000000000

		if(ServletFileUpload.isMultipartContent(request)){
			File uploadDir = new File(path+savePath);//new一个file 路径为rootPath-savePath
			if(!uploadDir.isDirectory()){
				uploadDir.mkdirs();
			}
			if(!uploadDir.canWrite()){//上传目录file是否有写入的权限
				resultMsg = "1";//上传目录没有写权限
			}else{
				String dirName = "upload";//设置上传目录为file
				if(!extMap.containsKey(dirName)){//判断上传目录是否正确
					resultMsg = "2";//目录名不正确
				}else{
					savePath += dirName + "/";
					File saveDirFile = new File(path+savePath);//c:/upload/
					if (!saveDirFile.exists()) {
						saveDirFile.mkdirs();
					}
					File dirFile = new File(path+savePath);//c:/upload/
					if (!dirFile.exists()) {
						dirFile.mkdirs();
					}
					DiskFileItemFactory factory = new DiskFileItemFactory();
					ServletFileUpload upload = new ServletFileUpload(factory);
					upload.setHeaderEncoding("UTF-8");
					List items = null;
					try {
						items = upload.parseRequest(request);
					} catch (FileUploadException e) {
						e.printStackTrace();
					}
					Iterator itr = items.iterator();
					while (itr.hasNext()) {
						FileItem item = (FileItem) itr.next();
						String fileName = item.getName();
						long fileSize = item.getSize();
						if (!item.isFormField()) {
							//检查文件大小
							if(item.getSize() > maxSize){
								resultMsg = "3";//上传文件大小超过限制
							}else{
								showDebug("[file_upload]fileName="+fileName);
								String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
								if(!Arrays.<String>asList(extMap.get(dirName).split(",")).contains(fileExt)){
									resultMsg = "4";//上传文件扩展名是不允许的扩展名
								}else{
									SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
									String newFileName = df.format(new Date()) + "_" + new Random().nextInt(1000) + "." + fileExt;
									try{
										File uploadedFile = new File(path+savePath, newFileName);
										item.write(uploadedFile);
										downloadUrl = savePath+newFileName;// /upload/newfilename
										resultMsg = "5";//上传文件成功
										//写数据库准备
										HashMap map=new HashMap();
										map.put("download_url",downloadUrl);// /upload/newfilename
										map.put("file_name",newFileName);  //newfilename
										map.put("file_path",path+savePath+newFileName); //c:/upload/newfilename
										jsonFile.add(map);
									}catch(Exception e){
										resultMsg = "6";//上传文件失败
									}
								}
							}
						}else{//如果获取的 表单信息是普通的 文本 信息
							//如果是FormField，就是前端的device_id,device_name这些
							String fieldName=item.getFieldName();
							String fieldValue=item.getString("UTF-8");
							showDebug("[upload_record][form_field]fieldName="+fieldName+"，fieldValue="+fieldValue);
							if(fieldName.equals("title")){title=fieldValue;};
							if(fieldName.equals("limit_time")){limitTime=fieldValue;};
						}
					}
				}
			}
		}
		showDebug("[upload_record]download_url="+downloadUrl+"&result_code="+resultCode+"&resultMsg="+resultMsg);
		//然后返回给前端
		json.put("result_code",0);
		json.put("result_msg","ok");
		json.put("files", jsonFile);
		json.put("download_url",downloadUrl);
	}
}
