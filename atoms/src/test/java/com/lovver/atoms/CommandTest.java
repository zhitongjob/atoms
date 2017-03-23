package com.lovver.atoms;

import com.lovver.atoms.broadcast.Command;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:applicationContext.xml"})
@TransactionConfiguration(defaultRollback = false)
public class CommandTest {

	@Test
	public void runTest() {

		for(int i=0;i<5;i++){
			Command cmd = new Command(Command.OPT_DELETE_KEY, "users", "ld"+i);
			byte[] bufs = cmd.toBuffers();
			System.out.print(cmd.getSrc() + ":");
			for(byte b : bufs){
				System.out.printf("[%s]",Integer.toHexString(b));			
			}
			System.out.println();
			Command cmd2 = Command.parse(bufs);
			System.out.printf("%d -> %d:%s:%s:%s:%d(%s)\n", cmd2.getSrc(), cmd2.getOperator(), cmd2.getRegion(), cmd2.getKey(),cmd2.getVal(),cmd2.getExpiretime(), cmd2.isLocalCommand());
		}
	}

}
