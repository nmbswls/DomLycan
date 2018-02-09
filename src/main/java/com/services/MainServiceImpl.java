package com.services;

import com.bean.Roles;
import com.dao.MainDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class MainServiceImpl{

    @Resource
    private MainDao mainDao;

    public int deal(List<String> list) {
        List<Roles> roles = new ArrayList<Roles>();
        for(int i=0;i<list.size();i++){
            roles.add(new Roles(2,list.get(i),i+1,"Y","Y"));
        }
        return mainDao.deal(roles);
    }
}
