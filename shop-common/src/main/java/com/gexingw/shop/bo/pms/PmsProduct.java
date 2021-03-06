package com.gexingw.shop.bo.pms;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gexingw.shop.bo.sys.SysUpload;
import com.gexingw.shop.constant.SystemConstant;
import com.gexingw.shop.constant.UploadConstant;
import com.gexingw.shop.mapper.UploadMapper;
import com.gexingw.shop.utils.SpringContextUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("pms_products")
public class PmsProduct {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String subTitle;

    private Long brandId;

    private Long categoryId;

    private String onSale;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer stock;  // 库存

    private Integer lowStock; // 预警库存

    private String unit; // 单位

    private String keywords;

    private String isNew;  // 新品

    private String isRecommend; // 推荐

    @JsonFormat(pattern = SystemConstant.DATETIME_STRING_FORMAT, timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @JsonFormat(pattern = SystemConstant.DATETIME_STRING_FORMAT, timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    public String getPic() {
        UploadMapper uploadMapper = SpringContextUtil.getBean(UploadMapper.class);
        SysUpload upload = uploadMapper.selectOne(new QueryWrapper<SysUpload>().eq("upload_id", id)
                .eq("upload_type", UploadConstant.UPLOAD_TYPE_PRODUCT));

        return upload != null ? upload.getFullUrl() : "";
    }
}
