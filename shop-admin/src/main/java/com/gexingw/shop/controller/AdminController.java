package com.gexingw.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gexingw.shop.bo.sys.SysUpload;
import com.gexingw.shop.bo.ums.UmsAdmin;
import com.gexingw.shop.dto.admin.UmsAdminRequestParam;
import com.gexingw.shop.dto.admin.UmsAdminSearchParam;
import com.gexingw.shop.enums.RespCode;
import com.gexingw.shop.mapper.UmsAdminMapper;
import com.gexingw.shop.service.*;
import com.gexingw.shop.utils.FileUtil;
import com.gexingw.shop.utils.PageUtil;
import com.gexingw.shop.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UmsAdminService umsAdminService;

    @Autowired
    private UmsAdminMapper umsAdminMapper;

    @Autowired
    UmsDeptService umsDeptService;

    @Autowired
    UmsMenuService umsMenuService;

    @Autowired
    UmsRoleService umsRoleService;

    @Autowired
    CommonService commonService;

    @GetMapping
    @PreAuthorize("@el.check('user:list')")
    public R index(UmsAdminSearchParam requestParams) {
        IPage<UmsAdmin> page = umsAdminMapper.queryList(new Page<>(requestParams.getPage(), requestParams.getSize()), requestParams);
        return R.ok(PageUtil.format(page));
    }

    @GetMapping("download")
    @PreAuthorize("@el.check('user:list')")
    public void download(UmsAdminSearchParam searchParams, HttpServletResponse response) throws IOException {
        IPage<UmsAdmin> page = umsAdminMapper.queryList(new Page<>(searchParams.getPage(), searchParams.getSize()), searchParams);
        List<UmsAdmin> records = page.getRecords();

        List<Map<String, Object>> list = new ArrayList<>();
        for (UmsAdmin umsAdmin : records) {
            HashMap<String, Object> item = new HashMap<>();
            item.put("ID", umsAdmin.getId());
            item.put("?????????", umsAdmin.getUsername());
            item.put("??????", umsAdmin.getNickName());
            item.put("??????", umsAdmin.getGender());
            item.put("??????", umsAdmin.getPhone());
            list.add(item);
        }

        FileUtil.downloadExcel(list, response);
    }

    @PostMapping()
    @PreAuthorize("@el.check('user:add')")
    public R add(@RequestBody UmsAdminRequestParam umsAdminRequestParam) {
        if (!umsAdminService.checkLevel(umsAdminRequestParam)) {
            return R.ok("???????????????");
        }

        return umsAdminService.save(umsAdminRequestParam) > 0 ? R.ok("???????????????") : R.ok("???????????????");
    }

    /**
     * ????????????
     *
     * @return
     */
    @PostMapping("/upload-avatar")
    public R upload(@RequestParam MultipartFile file, @RequestParam String uploadType, @RequestParam Long uploadId) {
        // ???????????????????????????????????????
        File uploadedFile = commonService.upload(file, uploadType);
        if (uploadedFile == null) {
            return R.ok("???????????????");
        }

        // ???????????????
        if (!commonService.detachOldFile(uploadId, uploadType)) {
            return R.ok(RespCode.DELETE_FAILURE.getCode(), "????????????????????????");
        }

        // ????????????????????????
        SysUpload upload = commonService.attachUploadFile(uploadId, uploadType, uploadedFile);
        if (upload == null) {
            return R.ok(RespCode.UPLOAD_FAILURE.getCode(), "???????????????");
        }

        return R.ok("???????????????");
    }

    @PutMapping()
    @PreAuthorize("@el.check('user:edit')")
    public R update(@RequestBody UmsAdminRequestParam umsAdminRequestParam) {
        if (!umsAdminService.checkLevel(umsAdminRequestParam)) {
            return R.ok("???????????????");
        }

        if (!umsAdminService.exist(umsAdminRequestParam.getId())) {
            return R.ok(RespCode.RESOURCE_NOT_EXIST.getCode(), "?????????????????????");
        }

        if (!umsAdminService.update(umsAdminRequestParam)) {
            return R.ok(RespCode.FAILURE.getCode(), "???????????????");
        }

        // ??????Redis??????????????????????????????????????????
        umsAdminService.delRedisAdminDetailByAdminId(umsAdminRequestParam.getId());
        umsMenuService.delRedisAdminMenuByAdminId(umsAdminRequestParam.getId());
        umsMenuService.delRedisAdminPermissionByAdminId(umsAdminRequestParam.getId());
        umsRoleService.delRedisAdminRolesByAdminId(umsAdminRequestParam.getId());
        umsAdminService.delRedisAdminDataScopeByAdminId(umsAdminRequestParam.getId());
        umsDeptService.delRedisAdminDeptByAdminId(umsAdminRequestParam.getId());

        return R.ok("???????????????");
    }

    @PutMapping("center")
    public R updateCenter(@RequestBody UmsAdminRequestParam requestParam) {
        try {
            if (!umsAdminService.updateCenter(requestParam)) {
                return R.ok(RespCode.FAILURE.getCode(), "???????????????");
            }
        } catch (Exception e) {
            return R.ok(RespCode.FAILURE.getCode(), e.getMessage());
        }

        return R.ok("???????????????");
    }

    @DeleteMapping()
    @PreAuthorize("@el.check('user:del')")
    public R destroy(@RequestBody Set<Long> ids) {
        if (!umsAdminService.delByIds(ids)) {
            return R.ok("???????????????");
        }

        for (Long id : ids) {
            umsRoleService.delRedisAdminRolesByAdminId(id);
            umsMenuService.delRedisAdminMenuByAdminId(id);
            umsMenuService.delRedisAdminPermissionByAdminId(id);
            umsAdminService.delRedisAdminDataScopeByAdminId(id);
            umsAdminService.delRedisAdminDetailByAdminId(id);
            umsDeptService.delRedisAdminDeptByAdminId(id);
        }

        return R.ok("???????????????");
    }

}

