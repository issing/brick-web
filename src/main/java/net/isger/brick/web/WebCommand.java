package net.isger.brick.web;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import net.isger.brick.ui.UICommand;
import net.isger.brick.ui.UIConstants;
import net.isger.util.Helpers;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public class WebCommand extends UICommand {

    private static final String NAME_INDEX = "index";

    private HttpServletRequest request;

    @Alias(WebConstants.BRICK_WEB_NAME)
    @Ignore(mode = Mode.INCLUDE)
    private String webName;

    @Alias(WebConstants.BRICK_ENCODING)
    @Ignore(mode = Mode.INCLUDE)
    private String encoding;

    // @Alias(WebConstants.BRICK_UPLOAD_THRESHOLD)
    // @Ignore(mode = Mode.INCLUDE)
    // private Integer uploadThreshold;
    //
    // @Alias(WebConstants.BRICK_UPLOAD_FILESIZE)
    // @Ignore(mode = Mode.INCLUDE)
    // private Long uploadFileSize;
    //
    // @Alias(WebConstants.BRICK_UPLOAD_REQUESTSIZE)
    // @Ignore(mode = Mode.INCLUDE)
    // private Long uploadRequestSize;
    //
    // @Alias(WebConstants.BRICK_UPLOAD_PATH)
    // @Ignore(mode = Mode.INCLUDE)
    // private String uploadPath;

    public WebCommand() {
        // this.uploadThreshold = 1024 * 1024 * 4; // 4M
        // this.uploadFileSize = 1024 * 1024 * 40l; // 40M
        // this.uploadRequestSize = 1024 * 1024 * 40l; // 40M
        // this.uploadPath = "upload";
    }

    /**
     * 初始命令
     * 
     * @param request
     * @param response
     * @param parameters
     */
    void initial(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> parameters) {
        this.request = request;
        makeTarget();
        makeParameters(parameters);
    }

    /**
     * 生成目标
     * 
     * @param request
     * @return
     */
    protected void makeTarget() {
        String domain;
        String contextPath = request.getContextPath().replaceAll("[/\\\\]+",
                "/");
        String path = request.getRequestURI().replaceAll("[/\\\\]+", "/");
        String[] pending = path.split("[:]");
        if (pending.length > 1) {
            domain = (contextPath.length() > 0
                    ? pending[0].substring(contextPath.length())
                    : pending[0]).replaceFirst("[/]", "");
            path = Strings.join(pending, 1);
        } else if (Strings.isEmpty(contextPath)) {
            domain = webName;
        } else {
            if (WebConstants.DEFAULT.equals(webName)) {
                domain = contextPath.replaceFirst("[/]", "");
            } else {
                domain = webName;
            }
            path = path.substring(contextPath.length());
        }
        this.setDomain(domain);

        pending = (String[]) Helpers.newArray(path.split("!"), 2);
        this.setName(Strings.empty(
                pending[0].replaceFirst("/", "").replaceAll("[/]", "."),
                NAME_INDEX));
        this.setOperate(Strings.empty(pending[1], UIConstants.OPERATE_SCREEN));
    }

    /**
     * 生成参数
     * 
     * @param parameters
     */
    protected void makeParameters(Map<String, Object> parameters) {
        String charset = request.getCharacterEncoding();
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            charset = "ISO-8859-1";
        }
        /* 获取请求参数 */
        Map<String, Object> result = new HashMap<String, Object>();
        if (parameters == null) {
            Enumeration<?> names = request.getParameterNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                result.put(name,
                        toEncoding(charset, request.getParameterValues(name)));
            }
            if (ServletFileUpload.isMultipartContent(request)) {
                // toMultipart();
                throw new IllegalStateException(
                        "Unimplements multipart process");
            }
        } else {
            Object value;
            for (Entry<String, Object> param : parameters.entrySet()) {
                value = param.getValue();
                if (value instanceof String[]) {
                    value = toEncoding(charset, (String[]) value);
                }
                result.put(param.getKey(), Helpers.compact(value));
            }
        }
        setParameter(result);
    }

    /**
     * 分部请求
     */
    // private void toMultipart() {
    // DiskFileItemFactory factory = new DiskFileItemFactory();
    // // 设置内存临界值 - 超过后将产生临时文件并存储于临时目录中
    // factory.setSizeThreshold(uploadThreshold);
    // // 设置临时存储目录
    // factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
    // // 文件上传参数设置
    // ServletFileUpload upload = new ServletFileUpload(factory);
    // upload.setFileSizeMax(uploadFileSize);
    // upload.setSizeMax(uploadRequestSize);
    // File uploadDirectory = new File(uploadPath);
    // if (!uploadDirectory.isAbsolute()) {
    // uploadDirectory = new File(uploadPath = request.getSession()
    // .getServletContext().getRealPath("./WEB-INF")
    // + File.separator + uploadPath);
    // }
    // if (!uploadDirectory.exists()) {
    // uploadDirectory.mkdirs();
    // }
    // try {
    // // 解析请求的内容提取文件数据
    // File storeFile;
    // List<FileItem> formItems = upload.parseRequest(request);
    // if (formItems.size() > 0) {
    // // 迭代表单数据
    // for (FileItem item : formItems) {
    // // 处理不在表单中的字段
    // if (!item.isFormField()) {
    // storeFile = new File(uploadDirectory, new File(
    // item.getName()).getName());
    // // 保存文件到硬盘
    // item.write(storeFile);
    // // TODO 多文件上传问题
    // setParameter(storeFile.getName(), storeFile);
    // }
    // }
    // }
    // } catch (Exception e) {
    // throw new IllegalStateException("Failure to process "
    // + request.getContentType(), e);
    // }
    // }

    private Object toEncoding(String charset, String... values) {
        int count = values.length;
        if (!encoding.equalsIgnoreCase(charset)) {
            for (int i = 0; i < count; i++) {
                values[i] = newString(charset, values[i]);
            }
        }
        return count == 1 ? values[0] : values;
    }

    private String newString(String charset, String value) {
        try {
            return new String(value.getBytes(charset), encoding);
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

}
