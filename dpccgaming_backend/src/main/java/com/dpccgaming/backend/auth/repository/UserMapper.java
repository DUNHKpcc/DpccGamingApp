package com.dpccgaming.backend.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dpccgaming.backend.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User>{

}
