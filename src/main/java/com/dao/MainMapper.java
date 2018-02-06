package com.dao;

import com.bean.Roles;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MainMapper {
    public Roles getRoleById(@Param("room_id")int roomId, @Param("player_number")int playerNumber);
    public int insertRoles(Roles roles);
    public int deleteRoles(int roomId);
}
