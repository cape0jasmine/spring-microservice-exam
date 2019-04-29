package com.github.tangyi.user.controller;

import com.github.pagehelper.PageInfo;
import com.github.tangyi.common.core.constant.CommonConstant;
import com.github.tangyi.common.core.model.ResponseBean;
import com.github.tangyi.common.core.utils.PageUtil;
import com.github.tangyi.common.core.utils.SysUtil;
import com.github.tangyi.common.core.web.BaseController;
import com.github.tangyi.common.log.annotation.Log;
import com.github.tangyi.common.security.constant.SecurityConstant;
import com.github.tangyi.common.security.utils.SecurityUtil;
import com.github.tangyi.user.api.module.Dept;
import com.github.tangyi.user.api.module.Role;
import com.github.tangyi.user.api.module.RoleDept;
import com.github.tangyi.user.service.DeptService;
import com.github.tangyi.user.service.RoleDeptService;
import com.github.tangyi.user.service.RoleMenuService;
import com.github.tangyi.user.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 角色controller
 *
 * @author tangyi
 * @date 2018/8/26 22:50
 */
@Api("角色信息管理")
@RestController
@RequestMapping("/v1/role")
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMenuService roleMenuService;

    @Autowired
    private RoleDeptService roleDeptService;

    @Autowired
    private DeptService deptService;

    /**
     * 根据id获取角色
     *
     * @param id id
     * @return Role
     * @author tangyi
     * @date 2018/9/14 18:20
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取角色信息", notes = "根据角色id获取角色详细信息")
    @ApiImplicitParam(name = "id", value = "角色ID", required = true, dataType = "String", paramType = "path")
    public Role role(@PathVariable String id) {
        try {
            return roleService.get(id);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new Role();
    }

    /**
     * 角色分页查询
     *
     * @param pageNum  pageNum
     * @param pageSize pageSize
     * @param sort     sort
     * @param order    order
     * @param role     role
     * @return PageInfo
     * @author tangyi
     * @date 2018/10/24 22:13
     */
    @RequestMapping("roleList")
    @ApiOperation(value = "获取角色列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "role", value = "角色信息", dataType = "Role")
    })
    public PageInfo<Role> userList(@RequestParam(value = CommonConstant.PAGE_NUM, required = false, defaultValue = CommonConstant.PAGE_NUM_DEFAULT) String pageNum,
                                   @RequestParam(value = CommonConstant.PAGE_SIZE, required = false, defaultValue = CommonConstant.PAGE_SIZE_DEFAULT) String pageSize,
                                   @RequestParam(value = CommonConstant.SORT, required = false, defaultValue = CommonConstant.PAGE_SORT_DEFAULT) String sort,
                                   @RequestParam(value = CommonConstant.ORDER, required = false, defaultValue = CommonConstant.PAGE_ORDER_DEFAULT) String order,
                                   Role role) {
        // 查询所属部门
        PageInfo<Role> pageInfo = roleService.findPage(PageUtil.pageInfo(pageNum, pageSize, sort, order), role);
        Stream<Role> roleStream = pageInfo.getList().stream();
        if (Optional.ofNullable(roleStream).isPresent()) {
            roleStream.forEach(tempRole -> {
                RoleDept roleDept = new RoleDept();
                roleDept.setRoleId(tempRole.getId());
                // 查询角色部门关系
                roleDept = roleDeptService.get(roleDept);
                if (roleDept != null) {
                    // 查询部门信息
                    Dept dept = new Dept();
                    dept.setId(roleDept.getDeptId());
                    dept = deptService.get(dept);
                    // 设置角色所属部门ID和名称
                    if (dept != null) {
                        tempRole.setDeptId(roleDept.getDeptId());
                        tempRole.setDeptName(dept.getDeptName());
                    }
                }
            });
        }
        return pageInfo;
    }

    /**
     * 根据部门ID获取角色
     *
     * @param deptId 部门ID
     * @return List
     */
    @GetMapping("/roleList/{deptId}")
    @ApiOperation(value = "获取角色信息", notes = "根据部门id获取角色详细信息")
    @ApiImplicitParam(name = "deptId", value = "部门ID", required = true, dataType = "String", paramType = "path")
    public List<Role> roleList(@PathVariable String deptId) {
        List<Role> roles = new ArrayList<>();
        if (StringUtils.isNotBlank(deptId)) {
            // 获取角色部门关系
            Stream<RoleDept> roleDeptStream = roleDeptService.getRoleByDeptId(deptId).stream();
            // 获取角色列表
            if (Optional.ofNullable(roleDeptStream).isPresent()) {
                Role role = new Role();
                // 流处理获取角色ID集合，去重，转成字符串数组
                role.setIds(roleDeptStream.map(RoleDept::getRoleId).distinct().toArray(String[]::new));
                roles = roleService.findListById(role);
            }
        }
        return roles;

    }

    /**
     * 修改角色
     *
     * @param role role
     * @return ResponseBean
     * @author tangyi
     * @date 2018/9/14 18:22
     */
    @PutMapping
    @PreAuthorize("hasAuthority('sys:role:edit') or hasAnyRole('" + SecurityConstant.ROLE_ADMIN + "', '" + SecurityConstant.ROLE_TEACHER + "')")
    @ApiOperation(value = "更新角色信息", notes = "根据角色id更新角色的基本信息")
    @ApiImplicitParam(name = "role", value = "角色实体role", required = true, dataType = "Role")
    @Log("修改角色")
    public ResponseBean<Boolean> updateRole(@RequestBody Role role) {
        role.setCommonValue(SecurityUtil.getCurrentUsername(), SysUtil.getSysCode());
        return new ResponseBean<>(roleService.update(role) > 0);
    }

    /**
     * 更新角色菜单
     *
     * @param role role
     * @return ResponseBean
     * @author tangyi
     * @date 2018/10/28 14:20
     */
    @PutMapping("roleMenuUpdate")
    @ApiOperation(value = "更新角色菜单信息", notes = "更新角色菜单信息")
    @ApiImplicitParam(name = "role", value = "角色实体role", required = true, dataType = "Role")
    @Log("更新角色菜单")
    public ResponseBean<Boolean> updateRoleMenu(@RequestBody Role role) {
        boolean success = false;
        String deptId = role.getDeptId();
        if (StringUtils.isNotBlank(role.getId())) {
            role = roleService.get(role);
            // 保存角色菜单关系
            if (role != null && StringUtils.isNotBlank(deptId))
                success = roleMenuService.saveRoleMenus(role.getId(), Stream.of(deptId.split(",")).collect(Collectors.toList())) > 0;
        }
        return new ResponseBean<>(success);
    }

    /**
     * 创建角色
     *
     * @param role role
     * @return ResponseBean
     * @author tangyi
     * @date 2018/9/14 18:23
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sys:role:add') or hasAnyRole('" + SecurityConstant.ROLE_ADMIN + "', '" + SecurityConstant.ROLE_TEACHER + "')")
    @ApiOperation(value = "创建角色", notes = "创建角色")
    @ApiImplicitParam(name = "role", value = "角色实体role", required = true, dataType = "Role")
    @Log("新增角色")
    public ResponseBean<Boolean> role(@RequestBody Role role) {
        role.setCommonValue(SecurityUtil.getCurrentUsername(), SysUtil.getSysCode());
        return new ResponseBean<>(roleService.insert(role) > 0);
    }

    /**
     * 根据id删除角色
     *
     * @param id id
     * @return Role
     * @author tangyi
     * @date 2018/9/14 18:24
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:del') or hasAnyRole('" + SecurityConstant.ROLE_ADMIN + "', '" + SecurityConstant.ROLE_TEACHER + "')")
    @ApiOperation(value = "删除角色", notes = "根据ID删除角色")
    @ApiImplicitParam(name = "id", value = "角色ID", required = true, paramType = "path")
    @Log("删除角色")
    public ResponseBean<Boolean> deleteRole(@PathVariable String id) {
        Role role = new Role();
        role.setId(id);
        role.setNewRecord(false);
        role.setCommonValue(SecurityUtil.getCurrentUsername(), SysUtil.getSysCode());
        return new ResponseBean<>(roleService.delete(role) > 0);
    }

    /**
     * 批量删除
     *
     * @param role role
     * @return ResponseBean
     * @author tangyi
     * @date 2018/12/4 10:00
     */
    @PostMapping("/deleteAll")
    @PreAuthorize("hasAuthority('sys:role:del') or hasAnyRole('" + SecurityConstant.ROLE_ADMIN + "', '" + SecurityConstant.ROLE_TEACHER + "')")
    @ApiOperation(value = "批量删除角色", notes = "根据角色id批量删除角色")
    @ApiImplicitParam(name = "role", value = "角色信息", dataType = "Role")
    @Log("批量删除角色")
    public ResponseBean<Boolean> deleteAllRoles(@RequestBody Role role) {
        boolean success = false;
        try {
            if (StringUtils.isNotEmpty(role.getIdString()))
                success = roleService.deleteAll(role.getIdString().split(",")) > 0;
        } catch (Exception e) {
            logger.error("删除角色失败！", e);
        }
        return new ResponseBean<>(success);
    }
}
