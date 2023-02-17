package net.isger.brick.web;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import net.isger.util.Files;

public class WebMultipart {

    private WebMultipart() {
    }

    public static Map<String, Object> parse(WebConfig config, HttpServletRequest request) throws Exception {
        if (!ServletFileUpload.isMultipartContent(request)) {
            return null;
        }
        /* 配置上传参数 */
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // 设置内存临界值 - 超过后将产生临时文件并存储于临时目录中
        factory.setSizeThreshold(config.getMemoryThreshold());
        // 设置临时存储目录
        factory.setRepository(Files.tmpdir());
        ServletFileUpload upload = new ServletFileUpload(factory);
        // 设置最大文件上传值
        upload.setFileSizeMax(config.getMaxFileSize());
        // 设置最大请求值 (包含文件和表单数据)
        upload.setSizeMax(config.getMaxRequestSize());
        // 中文处理
        upload.setHeaderEncoding(config.getEncoding());

        /* 解析请求的内容提取文件数据 */
        List<FileItem> formItems = upload.parseRequest(request);
        if (formItems != null && formItems.size() > 0) {
            // 迭代表单数据
            for (FileItem item : formItems) {
                // 处理不在表单中的字段
                if (!item.isFormField()) {
                    item.write(File.createTempFile(WebConstants.BRICK_WEB_UPLOAD, item.getFieldName()));
                }
            }
        }
        return null;
    }

}
