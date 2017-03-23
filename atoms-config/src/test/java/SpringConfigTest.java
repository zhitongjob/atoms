import com.lovver.atoms.config.AtomsBean;
import com.lovver.atoms.config.AtomsConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class SpringConfigTest {

	public static void main(String[] args) {
		ApplicationContext ac=new ClassPathXmlApplicationContext(new String[]{"classpath:applicationContext.xml"});
		AtomsBean config=AtomsConfig.getAtomsConfig();
		System.out.println(config.getSerializer());
	}

}
