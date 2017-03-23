import com.lovver.atoms.config.AtomsBean;
import com.lovver.atoms.config.AtomsConfig;

/**
 * Created by Administrator on 2017/3/23.
 */
public class AtomsConfigTest {


    public static void main(String args[]){
        AtomsBean config= AtomsConfig.getAtomsConfig();
        System.out.println(config.getSerializer());
    }
}
