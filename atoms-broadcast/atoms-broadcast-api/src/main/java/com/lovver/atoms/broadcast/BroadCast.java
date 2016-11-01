package com.lovver.atoms.broadcast;

import com.lovver.atoms.common.annotation.Extension;
import com.lovver.atoms.config.AtomsBroadCastBean;

@Extension("spi")
public interface BroadCast extends Cloneable{
	
	public void init(AtomsBroadCastBean atomBean);
	
	public void broadcast(String message);
	
//	public int getLevel();
	
}
