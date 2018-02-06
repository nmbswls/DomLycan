package com.dao;

import com.bean.Roles;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class MainDao {

    @Resource
    private SqlSessionFactory sqlSessionFactory;


    public int getEmptyRoom(){
        SqlSession session = null;
        try{
            session = sqlSessionFactory.openSession();
            session.close();
        }catch (Exception e){

        }finally {

        }
        return 0;
    }

    public int deal(List<Roles> list){
        SqlSession session = null;
        int success = 0;
        try{

            session = sqlSessionFactory.openSession();
            MainMapper mainMapper = session.getMapper(MainMapper.class);
            for(int i=0;i<list.size();i++)
                success+=mainMapper.insertRoles(list.get(i));

        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }
        return success;
    }

}
