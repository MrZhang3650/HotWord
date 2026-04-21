package com.zcm.hotwordinsight.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zcm.hotwordinsight.entity.Commodity;
import com.zcm.hotwordinsight.response.R;
import com.zcm.hotwordinsight.service.CommodityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 */
@RestController
@Tag(name = "商品管理")
public class CommodityController {
    @Resource
    private CommodityService commodityService;

    @Operation(summary = "获取商品列表")
    @PostMapping("/commodity/list")
    @SaCheckLogin
    @CrossOrigin
    public R<PageInfo<Commodity>> list(@RequestBody Commodity commodity, @RequestParam Integer pageNum, @RequestParam Integer pageSize){
        LambdaQueryWrapper<Commodity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(commodity.getTitle() != null, Commodity::getTitle, commodity.getTitle());
        wrapper.like(commodity.getKeyword() != null, Commodity::getKeyword, commodity.getKeyword());
        wrapper.like(commodity.getCategory() != null, Commodity::getCategory, commodity.getCategory());
        wrapper.like(commodity.getSource() != null, Commodity::getSource, commodity.getSource());
        wrapper.orderByDesc(Commodity::getId);
        PageHelper.startPage(pageNum, pageSize);
        List<Commodity> list = commodityService.list(wrapper);
        PageInfo<Commodity> pageInfo = new PageInfo(list);
        return R.data(pageInfo);
    }

    @Operation(summary = "根据ID获取商品详情")
    @PostMapping("/commodity/detail")
    @SaCheckLogin
    @CrossOrigin
    public R<Commodity> detail(@RequestParam Long id){
        Commodity commodity = commodityService.getById(id);
        return R.data(commodity);
    }

    @Operation(summary = "添加商品")
    @PostMapping("/commodity/add")
    @SaCheckLogin
    @CrossOrigin
    public R add(@RequestBody Commodity commodity){
        commodityService.save(commodity);
        return R.success("添加商品成功！");
    }

    @Operation(summary = "修改商品")
    @PostMapping("/commodity/update")
    @SaCheckLogin
    @CrossOrigin
    public R update(@RequestBody Commodity commodity){
        commodityService.updateById(commodity);
        return R.success("修改商品成功！");
    }

    @Operation(summary = "删除商品")
    @PostMapping("/commodity/delete")
    @SaCheckLogin
    @CrossOrigin
    public R delete(@RequestParam List<Long> ids){
        commodityService.removeByIds(ids);
        return R.success("删除商品成功！");
    }
}
