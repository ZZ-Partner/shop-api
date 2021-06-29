package com.gexingw.shop.controller;

import com.gexingw.shop.enums.RespCode;
import com.gexingw.shop.service.CommonService;
import com.gexingw.shop.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;

@RestController
@RequestMapping("common")
public class CommonController {

    @Autowired
    CommonService commonService;

    /**
     * 上传接口
     *
     * @return
     */
    @PostMapping("upload")
    public R upload(@RequestParam MultipartFile file, @RequestParam String uploadType, @RequestParam Long uploadId) {
        // 删除旧文件
        if (!commonService.detachOldFile(uploadId, uploadType)) {
            return R.ok(RespCode.DELETE_FAILURE.getCode(), "旧图片删除失败！");
        }

        // 上传文件获取服务器文件路径
        File uploadedFile = commonService.upload(file, uploadType);

        // 资源与新图片绑定
        String uploadPath = commonService.attachNewFile(uploadId, uploadType, uploadedFile);
        if (uploadPath == null) {
            return R.ok(RespCode.SAVE_FAILURE.getCode(), "新图片绑定失败！");
        }

        HashMap<String, Object> result = new HashMap<>();
        result.put("path", uploadPath);

        return R.ok(result, "上传成功！");
    }

}
