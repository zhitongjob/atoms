import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * Created by Administrator on 2017/3/15.
 */
public class TestEhcache {
    public static void main(String args[]) throws InterruptedException {
        CacheManager ehCacheManager = new CacheManager();
        ehCacheManager.addCache("default-111");
        Ehcache cache = ehCacheManager.getCache("default-111");
        Element e = new Element("aa", "aa", false, 1, 1);
        cache.put(e);
        System.out.println(cache.get("aa"));
        Thread.sleep(1050);
        System.out.println(cache.get("aa"));
    }
}
