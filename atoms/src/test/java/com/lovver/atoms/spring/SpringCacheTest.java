package com.lovver.atoms.spring;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Administrator on 2017/3/20.
 */
public class SpringCacheTest {

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext context=new ClassPathXmlApplicationContext(new String[]{"classpath:applicationContext-springcache.xml"});

        UserService userService = context.getBean(UserService.class);
        // 第一次查询，应该走数据库
        System.out.print("第一次查询...");
        userService.getUserByName("hello");
        // 第二次查询，应该不查数据库，直接返回缓存的值
        System.out.println("第二次查询...");
        userService.getUserByName("hello");
        System.out.println();
        System.out.println("==============");

        // 更新某个记录的缓存，首先构造两个用户记录，然后记录到缓存中
        User user1 = userService.getUserByName("user1");
        // 开始更新其中一个
        user1.setId(1000);
        userService.updateUser(user1);
        // 因为被更新了，所以会查询数据库
        userService.getUserByName("user1");
        // 再次查询，应该走缓存
        userService.getUserByName("user1");
        // 更新所有缓存
        userService.reload();
        System.out.println("清楚所有缓存");
        // 查询数据库
        userService.getUserByName("user1");
        userService.getUserByName("user2");
        // 查询缓存
        userService.getUserByName("user1");
        userService.getUserByName("user2");

    }
}
