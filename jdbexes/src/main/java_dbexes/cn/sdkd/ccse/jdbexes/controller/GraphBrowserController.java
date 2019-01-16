package cn.sdkd.ccse.jdbexes.controller;

import cn.sdkd.ccse.jdbexes.service.INeo4jService;
import com.wangzhixuan.commons.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by sam on 2019/1/14.
 */
@Controller
@RequestMapping("/graph")
public class GraphBrowserController extends BaseController {
    @Autowired
    private INeo4jService neo4jService;

    @GetMapping("/browser")
    public String manager() {
        return "graph/browser";
    }

    @RequestMapping(value = "/getExperiments")
    @ResponseBody
    public Object getExperiments() {
        return neo4jService.getExperiments();
    }

    @RequestMapping(value = "/getStudents")
    @ResponseBody
    public Object getStudents() {
        return neo4jService.getStudents();
    }

}
