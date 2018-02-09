package com.dao;

import com.bean.Roles;
import org.apache.ibatis.annotations.Param;


public interface MainMapper {
    public Roles getRoleById(@Param("roomId") int roomId, @Param("playerNumber") int playerNumber);
    public int insertRoles(Roles roles);
    public int deleteRoles(int roomId);
}
