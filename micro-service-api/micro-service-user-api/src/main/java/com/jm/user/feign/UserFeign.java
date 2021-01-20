package com.jm.user.feign;

import com.jm.user.pojo.User;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@FeignClient(value = "user")
@RequestMapping(value="/user")
public interface UserFeign {

    /***
     * 根据ID查询User数据
     * @param id
     * @return
     */
    @GetMapping({"/load/{id}"})
    public Result<User> findById(@PathVariable String id);
}
