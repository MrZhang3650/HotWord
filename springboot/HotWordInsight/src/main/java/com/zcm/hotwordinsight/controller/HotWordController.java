package com.zcm.hotwordinsight.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zcm.hotwordinsight.entity.HotWord;
import com.zcm.hotwordinsight.exception.BussinessException;
import com.zcm.hotwordinsight.response.R;
import com.zcm.hotwordinsight.response.ResponseCode;
import com.zcm.hotwordinsight.service.HotWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author： ZhangChenMing
 * @date： 周三 2026-3-18
 * @description：
 * @modifiedBy：
 * @version:
 */
@RestController
@Tag(name = "热词相关信息管理")
public class HotWordController {
    @Resource
    public HotWordService hotWordService;

    @Operation(summary = "添加热词相关信息")
    @PostMapping("/hotword/add")
    @CrossOrigin
    public R add(@RequestBody HotWord hotWord){
        LambdaQueryWrapper<HotWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotWord::getKeyword, hotWord.getKeyword());
        long count = hotWordService.count(wrapper);
        if (count > 0){
            throw new BussinessException(ResponseCode.KEYWORD_EXIST);
        }
        hotWordService.save(hotWord);
        return R.success("添加成功！");
    }

    @Operation(summary = "查询热词相关信息")
    @PostMapping("/hotword/list")
    @CrossOrigin
    public R<PageInfo<HotWord>> list(@RequestBody HotWord hotWord, @RequestParam Integer pageNum, @RequestParam Integer pageSize){
        LambdaQueryWrapper<HotWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(hotWord.getKeyword()!=null, HotWord::getKeyword, hotWord.getKeyword());
        wrapper.like(hotWord.getSource()!=null, HotWord::getSource, hotWord.getSource());
        wrapper.orderByDesc(HotWord::getId);
        PageHelper.startPage(pageNum, pageSize);
        List<HotWord> list = hotWordService.list(wrapper);
        PageInfo<HotWord> pageInfo = new PageInfo(list);
        return R.data(pageInfo);
    }

    @Operation(summary = "修改热词相关信息")
    @PostMapping("/hotword/update")
    @CrossOrigin
    public R update(@RequestBody HotWord hotWord){
        hotWordService.updateById(hotWord);
        return R.success("修改成功！");
    }

    @Operation(summary = "通过id删除热词相关信息")
    @PostMapping("/hotword/del")
    @CrossOrigin
    public R del(@RequestParam Long id){
        hotWordService.removeById(id);
        return R.success("删除成功！");
    }

    @Operation(summary = "根据ID获取热词详情")
    @PostMapping("/hotword/detail")
    @CrossOrigin
    public R<HotWord> detail(@RequestParam Long id){
        HotWord hotWord = hotWordService.getById(id);
        return R.data(hotWord);
    }
}
