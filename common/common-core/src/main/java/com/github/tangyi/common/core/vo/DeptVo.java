package com.github.tangyi.common.core.vo;

import com.github.tangyi.common.core.persistence.BaseEntity;

/**
 * 部门vo
 *
 * @author tangyi
 * @date 2018/12/31 22:02
 */
public class DeptVo extends BaseEntity<DeptVo> {

    private String deptName;

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
}
