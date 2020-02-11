package com.wangzhixuan.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.wangzhixuan.model.User;
import com.wangzhixuan.model.vo.UserVo;
import org.apache.ibatis.annotations.Select;

/**
 *
 * User 表数据库控制层接口
 *
 */
public interface UserMapper extends BaseMapper<User> {

    UserVo selectUserVoById(@Param("id") Long id);

    List<Map<String, Object>> selectUserPage(Pagination page, Map<String, Object> params);

    @Select("SELECT u.id, u.login_name as loginName, u.name FROM User u WHERE u.organization_id = #{organization_id} and user_type = 1")
    List<User> selectStudentByOrganizationId(@Param("organization_id") Long organization_id);

}