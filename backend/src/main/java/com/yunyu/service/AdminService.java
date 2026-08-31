package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunyu.dao.AdminDao;
import com.yunyu.entity.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private AdminDao adminDao;

    public Admin login(String loginAct, String loginPwd) {
        Admin admin = adminDao.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getAccount, loginAct));
        if (admin == null) {
            throw new IllegalArgumentException("管理员账号不存在");
        }
        String md5Pwd = DigestUtils.md5DigestAsHex(loginPwd.getBytes());
        if (!admin.getPassword().equals(md5Pwd)) {
            throw new IllegalArgumentException("密码错误");
        }
        return admin;
    }

    public List<Admin> listAdmins() {
        return adminDao.selectList(new LambdaQueryWrapper<Admin>()
                .orderByDesc(Admin::getCreateTime));
    }

    public void createAdmin(String account, String password, String name, Integer role) {
        Admin exist = adminDao.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getAccount, account));
        if (exist != null) {
            throw new IllegalArgumentException("管理员账号已存在");
        }
        Admin admin = new Admin();
        admin.setAccount(account);
        admin.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        admin.setName(name);
        admin.setRole(role != null ? role : 0);
        adminDao.insert(admin);
    }

    public void deleteAdmin(Integer id, Integer currentAdminId) {
        if (id.equals(currentAdminId)) {
            throw new IllegalArgumentException("不能删除自己的账号");
        }
        Admin admin = adminDao.selectById(id);
        if (admin == null) {
            throw new IllegalArgumentException("管理员不存在");
        }
        if (admin.getRole() == 1) {
            throw new IllegalArgumentException("不能删除最终管理员");
        }
        adminDao.deleteById(id);
    }

    public void changePassword(Integer adminId, String oldPwd, String newPwd) {
        Admin admin = adminDao.selectById(adminId);
        if (admin == null) {
            throw new IllegalArgumentException("管理员不存在");
        }
        String oldMd5 = DigestUtils.md5DigestAsHex(oldPwd.getBytes());
        if (!admin.getPassword().equals(oldMd5)) {
            throw new IllegalArgumentException("原密码错误");
        }
        admin.setPassword(DigestUtils.md5DigestAsHex(newPwd.getBytes()));
        adminDao.updateById(admin);
    }
}
