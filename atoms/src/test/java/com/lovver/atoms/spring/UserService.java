package com.lovver.atoms.spring;


import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 * 业务服务
 *
 */
public class UserService {

    @Cacheable(value = "userCache",key="#userName")
    // 使用了一个缓存名叫 userCache
    public User getUserByName(String userName) {
        // 方法内部实现不考虑缓存逻辑，直接实现业务
        return getFromDB(userName);
    }

    @CacheEvict(value = "userCache", key = "#user.name")
    // 清空 accountCache 缓存
    public void updateUser(User user) {
        updateDB(user);
    }

    @CacheEvict(value = "userCache", allEntries = true,beforeInvocation=true)
    // 清空 accountCache 缓存
    public void reload() {
    }

    private User getFromDB(String userName) {
        System.out.println("查询数据库..." + userName);
        return new User(userName);
    }

    private void updateDB(User user) {
        System.out.println("更新数据库数据..." + user.getName());
    }
}
