package com.lovver.atoms.broadcast;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import com.lovver.atoms.common.utils.ReflectionUtils;
import com.lovver.atoms.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 命令消息封装
 * 格式：
 * 第1个字节为命令代码，长度1 [OPT]
 * 第2、3个字节为region长度，长度2 [R_LEN]
 * 第4、N 为 region 值，长度为 [R_LEN]
 * 第N+1、N+2 为 key 长度，长度2 [K_LEN]
 * 第N+3、M为 key值，长度为 [K_LEN]
 * 
 * @author winterlau
 */
public class Command {		

	private static Serializer serialize = ReflectionUtils.invokeStaticMethod("com.lovver.atoms.context.AtomsContext","getSerializer");
	private final static Logger log = LoggerFactory.getLogger(Command.class);
	
	private final static int SRC_ID = genRandomSrc(); //命令源标识，随机生成

	public final static byte OPT_DELETE_KEY = 0x01; 	//删除缓存
	public final static byte OPT_CLEAR_KEY = 0x02; 		//清除缓存
	public final static byte OPT_PUT_KEY = 0x03; 		//设置缓存
	
	private int src;
	private byte operator;
	private String region;
	private Object key;
	private Object val;
	private int expiretime;
	
	private static int genRandomSrc() {
		long ct = System.currentTimeMillis();
		Random rnd_seed = new Random(ct);
		return (int)(rnd_seed.nextInt(10000) * 1000 + ct % 1000);
	}

//	public static void main(String[] args) {
//
//		for(int i=0;i<5;i++){
//			Command cmd = new Command(OPT_DELETE_KEY, "users", "ld"+i,"val"+i);
//			byte[] bufs = cmd.toBuffers();
//			System.out.print(cmd.getSrc() + ":");
//			for(byte b : bufs){
//				System.out.printf("[%s]",Integer.toHexString(b));
//			}
//			System.out.println();
//			Command cmd2 = Command.parse(bufs);
//			System.out.printf("%d -> %d:%s:%s:%s(%s)\n", cmd2.getSrc(), cmd2.getOperator(), cmd2.getRegion(), cmd2.getKey(),cmd2.getVal(), cmd2.isLocalCommand());
//		}
//	}

	public Command(byte o, String r, Object k){
		this.operator = o;
		this.region = r;
		this.key = k;
		this.src = SRC_ID;
	}

	public Command(byte o, String r, Object k,Object v){
		this.operator = o;
		this.region = r;
		this.key = k;
		this.src = SRC_ID;
		this.val=v;
	}

    public Command(byte o, String r, Object k,Object v,int expiretime){
        this.operator = o;
        this.region = r;
        this.key = k;
        this.src = SRC_ID;
        this.val=v;
        this.expiretime=expiretime;
    }
	
	public byte[] toBuffers(){
		byte[] keyBuffers = null;
		try {
			keyBuffers = serialize.serialize(key);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
        byte[] valBuffers = null;
        try {
            if(val!=null) {
                valBuffers = serialize.serialize(val);
            }
        } catch (IOException e) {
            return null;
        }

		int r_len = region.getBytes().length;
		int k_len = keyBuffers.length;

		int v_len=0;
		if(valBuffers!=null){
		    v_len=valBuffers.length;
        }

		byte[] buffers = new byte[11 + r_len + k_len+4+v_len+4];
		int idx = 0;
		System.arraycopy(int2bytes(this.src), 0, buffers, idx, 4);
		idx += 4;
		buffers[idx] = operator;
		idx += 1;
		System.arraycopy(int2bytes(r_len), 0, buffers, idx, 2);
		idx += 2;
		System.arraycopy(region.getBytes(), 0, buffers, idx, r_len);
		idx += r_len;
		System.arraycopy(int2bytes(k_len), 0, buffers, idx, 4);
		idx += 4;
		System.arraycopy(keyBuffers, 0, buffers, idx, k_len);
		if(valBuffers!=null) {
            idx += k_len;
            System.arraycopy(int2bytes(v_len), 0, buffers, idx, 4);
            idx += 4;
            System.arraycopy(valBuffers, 0, buffers, idx, v_len);
            idx += v_len;
            System.arraycopy(int2bytes(expiretime), 0, buffers, idx, 4);
        }

		return buffers;
	}
	
	public static Command parse(byte[] buffers) {
		Command cmd = null;
		try{
			int idx = 4;
			byte opt = buffers[idx++];
			int r_len = bytes2int(new byte[]{buffers[idx++], buffers[idx++], 0, 0});
			if(r_len > 0){
				String region = new String(buffers, idx, r_len);
				idx += r_len;
				int k_len = bytes2int(Arrays.copyOfRange(buffers, idx, idx + 4));
				idx += 4;
				if(k_len > 0){
					//String key = new String(buffers, idx, k_len);
					byte[] keyBuffers = new byte[k_len];
					System.arraycopy(buffers, idx, keyBuffers, 0, k_len);
					Object key = serialize.deserialize(keyBuffers);
					cmd = new Command(opt, region, key);
					cmd.src = bytes2int(buffers);
					idx+=k_len;
				}
				int v_len=bytes2int(Arrays.copyOfRange(buffers, idx, idx + 4));
                idx += 4;
                if(v_len>0){
                    byte[] valBuffers = new byte[v_len];
                    System.arraycopy(buffers, idx, valBuffers, 0, v_len);
                    Object val = serialize.deserialize(valBuffers);
                    cmd.val=val;
                    idx+=v_len;
                    cmd.expiretime=bytes2int(Arrays.copyOfRange(buffers, idx, idx + 4));
                }
			}
		}catch(Exception e){
			log.error("Unabled to parse received command.", e);
		}
		return cmd;
	}
	
	private static byte[] int2bytes(int i) {
        byte[] b = new byte[4];

        b[0] = (byte) (0xff&i);
        b[1] = (byte) ((0xff00&i) >> 8);
        b[2] = (byte) ((0xff0000&i) >> 16);
        b[3] = (byte) ((0xff000000&i) >> 24);
        
        return b;
	}
	
	private static int bytes2int(byte[] bytes) {
		int num = bytes[0] & 0xFF;
		num |= ((bytes[1] << 8) & 0xFF00);
		num |= ((bytes[2] << 16) & 0xFF0000);
		num |= ((bytes[3] << 24) & 0xFF000000);
		return num;
	}
	
	public boolean isLocalCommand() {
		return this.src == SRC_ID;
	}
	
	public byte getOperator() {
		return operator;
	}

	public void setOperator(byte operator) {
		this.operator = operator;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public int getSrc() {
		return src;
	}

    public Object getVal() {
        return val;
    }

    public void setVal(Object val) {
        this.val = val;
    }

    public int getExpiretime() {
        return expiretime;
    }

    public void setExpiretime(int expiretime) {
        this.expiretime = expiretime;
    }
}
