package cn.sdkd.ccse.jdbexes.controller;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.service.INeo4jService;
import com.wangzhixuan.commons.base.BaseController;
import com.wangzhixuan.model.Organization;
import com.wangzhixuan.service.IOrganizationService;
import com.wangzhixuan.service.IUserService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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

    @RequestMapping("/browser")
    public String manager(Model model) {
        List<Student> students = neo4jService.getStudents();
        List<Experiment> experiments = neo4jService.getExperiments();
        List<Organization> organizations = organizationService.selectTreeGrid();

        model.addAttribute("students", students);
        model.addAttribute("experiments", experiments);
        model.addAttribute("organizations", organizations);

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
    public Object findBySimValueExperimentidStudentid(@Param("simValue") Float simValue, @Param("expid") Long expid, @Param("stuid")Long stuid) {
        return neo4jService.findSimilaritiesBySimValueExperimentidStudentid(simValue, expid, stuid);
    }

    @RequestMapping(value = "/getStudentIdByOrganizationId")
    @ResponseBody
    public Object findStudentByOrganizationId(@Param("organization_id") Long organization_id) {
        return userService.selectStudentIdByOrganizationId(organization_id);
    }
}
