package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传和下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    //文件上传
    @PostMapping("upload")
    public R<String> upload(MultipartFile file){//注意这里参数名称要和前端上传主键那个name属性一致
        log.info(file.toString());
        /**
         * 对上传名字处理
         */
        //原始文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));//截取原始文件名后缀名
        //使用UUID随机重新生成文件名，防止文件名称重复造成文件覆盖
        String filename = UUID.randomUUID().toString()+suffix;//动态拼接

        /**
         * 没有目录可以自动创建
         */
        //创建一个目录对象
        File dir = new File(basePath);
        //判断当前目录是否存在
        if (!dir.exists()){
            dir.mkdirs();
        }


        //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        try {
            file.transferTo(new File(basePath+filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(filename);//返回文件名称
    }


    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("download")
    public void download(String name, HttpServletResponse response){

        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath+name));

            //输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();//从次响应体中获取输出流，往浏览器写

            response.setContentType("image/jpeg");//因为下载的是图片，设置响应回去的文件类型是图片

            //定义bytes数组，长度是1024
            int len = 0;
            byte[] bytes = new byte[1024];
            while ( (len = fileInputStream.read(bytes)) !=-1 ){//输入流一直读，读了放到bytes数组，直到读完
                outputStream.write(bytes,0,len);//边读边写，从第一位开始读，长度为1024
                outputStream.flush();//读一次，写一次，就刷新一下
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
