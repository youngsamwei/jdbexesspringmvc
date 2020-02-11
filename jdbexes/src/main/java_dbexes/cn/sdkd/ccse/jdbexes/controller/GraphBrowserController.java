package cn.sdkd.ccse.jdbexes.controller;

import cn.sdkd.ccse.jdbexes.service.IExperimentService;
import cn.sdkd.ccse.jdbexes.service.INeo4jService;
import com.wangzhixuan.commons.base.BaseController;
import com.wangzhixuan.service.IOrganizationService;
import com.wangzhixuan.service.IUserService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    @Autowired
    private IOrganizationService organizationService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IExperimentService experimentService;

    @RequestMapping("/browser")
    public String manager(Model model) {
        return "graph/browser";
    }

    @RequestMapping(value = "/getExperiments")
    @ResponseBody
    public Object getExperiments() {
        return experimentService.selectAll();
    }

    @RequestMapping(value = "/getStudents")
    @ResponseBody
    public Object getStudents() {
        return userService.selectAllStudent();
    }

    @RequestMapping(value = "/getOrganizations")
    @ResponseBody
    public Object getOrganizations() {
        return organizationService.selectTreeGrid();
    }

    @RequestMapping(value = "/getSimilarities")
    @ResponseBody
    public Object getSimilarities(@Param("simValue") Float simValue) {
        return neo4jService.getSimilarities(simValue);
    }

    @RequestMapping(value = "/getSimilaritiesBySimValueExperimentid")
    @ResponseBody
    public Object findBySimValueExperimentid(@Param("simValue") Float simValue, @Param("expid") Long expid) {
        return neo4jService.findSimilaritiesBySimValueExperimentid(simValue, expid);
    }

    @RequestMapping(value = "/getSimilaritiesBySimValueStudentid")
    @ResponseBody
    public Object findBySimValueStudentid(@Param("simValue") Float simValue, @Param("stuid") Long stuid) {
        return neo4jService.findSimilaritiesBySimValueStudentid(simValue, stuid);
    }

    @RequestMapping(value = "/getSimilaritiesBySimValueExperimentidStudentid")
    @ResponseBody
    public Object findBySimValueExperimentidStudentid(@Param("simValue") Float simValue, @Param("expid") Long expid, @Param("stuid") Long stuid) {
        return neo4jService.findSimilaritiesBySimValueExperimentidStudentid(simValue, expid, stuid);
    }

    @RequestMapping(value = "/getStudentIdByOrganizationId")
    @ResponseBody
    public Object findStudentByOrganizationId(@Param("organization_id") Long organization_id) {
        return userService.selectStudentByOrganizationId(organization_id);
    }
}
