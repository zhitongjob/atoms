import com.lovver.atoms.config.AtomsBean;
import com.lovver.atoms.config.AtomsConfig;


public class ConfigTest {

	public static void main(String[] args) {
		AtomsBean config=AtomsConfig.getAtomsConfig();
		System.out.println(config.getSerializer());
	}

}
