package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request,
                             @RequestBody Employee employee) {
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 查询数据库账号密码/账号禁用状态
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 错误处理
        if (null == emp) return R.error("user does not exist");
        if (!emp.getPassword().equals(password)) return R.error("password error");
        if (emp.getStatus() == 0) return R.error("account forbidden");

        // 登录成功,下发一个empId给浏览器,存到localhost作为之后登录的依据
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }


    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }


    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());

        //设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户的id
        Long empId = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }


    @GetMapping("/page")
    // name是一个条件查询条件
    public R<Page> page(@RequestParam(name="page") int pageNum, int pageSize, String name) {
        log.info("pageNum = {}, pageSize = {}, name = {}", pageNum, pageSize, name);
        // 新建一个分页查询对象
        Page pageInfo = new Page(pageNum, pageSize);
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件,使用like(condition, col, queryValue)
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 再次添加一个排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 执行查询之后会自动返回,不需要自己手动返回
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }


    @PutMapping()
    public R<String> update(HttpServletRequest request,
                            @RequestBody Employee employee) {
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        // 当前操作人从浏览器的session获取
        employee.setUpdateUser(empId);
        employeeService.updateById(employee);   // 继承自mybatis的IService接口
        return R.success("");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }
}
