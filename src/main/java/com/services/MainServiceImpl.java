package com.services;

import com.bean.Roles;
import com.dao.MainDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MainServiceImpl{

    @Resource
    private MainDao mainDao;

    public int deal(List<Roles> list) {
        return mainDao.deal(list);
    }
}
