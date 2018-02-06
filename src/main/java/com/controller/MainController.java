package com.controller;

import com.bean.Roles;
import com.services.MainService;
import com.services.MainServiceImpl;
import javafx.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class MainController {

//    @RequestParam("name") String name,

    @Resource
    private MainServiceImpl mainServiceImpl;

    @RequestMapping("/rng")
    public String rng( Model model){



        return "index";
    }
    @RequestMapping("/rngs")
    public String rngs(@RequestParam(name = "village",defaultValue = "4")Integer village,
                       @RequestParam(name = "lycan",defaultValue = "4")Integer lycan,
                       @RequestParam(name = "defaults",defaultValue = "new String[0]")String[] defaults,
                       @RequestParam(name = "customs",defaultValue = "new String[0]")String[] customs, Model model){
        int totalPlayer = village + lycan;
        List<String> rolesOrigin = new ArrayList<String>();
        for(int i=0;i<village;i++)rolesOrigin.add("村民");
        for(int i=0;i<lycan;i++)rolesOrigin.add("狼人");

        for(int i =0;i<customs.length;i++){
            rolesOrigin.add(customs[i]);
            totalPlayer++;
        }
        for(int i =0;i<defaults.length;i++){
            rolesOrigin.add(defaults[i]);
            totalPlayer++;
        }


        Collections.shuffle(rolesOrigin);
        for(int i=0;i<rolesOrigin.size();i++){
            System.out.println(rolesOrigin.get(i)+" ");
        }
        return "index";
    }
}
