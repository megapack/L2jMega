package com.elfocrash.roboto.ai.walker;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.model.WalkNode;
import com.elfocrash.roboto.model.WalkerType;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;

public class GiranWalkerAI extends WalkerAI {
	
	public GiranWalkerAI(FakePlayer character) {
		super(character);
	}
	
	@Override
	protected WalkerType getWalkerType() {
		return WalkerType.RANDOM;
	}
	
	@Override
	protected void setWalkNodes() {
		_walkNodes.add(new WalkNode(Config.LOC_X, Config.LOC_Y, Config.LOC_Z, Rnd.get(1, 20)));
		_walkNodes.add(new WalkNode(Config.LOC_X1, Config.LOC_Y1, Config.LOC_Z1, Rnd.get(1, 20)));
		_walkNodes.add(new WalkNode(Config.LOC_X2, Config.LOC_Y2, Config.LOC_Z2, Rnd.get(1, 20)));
		_walkNodes.add(new WalkNode(Config.LOC_X3, Config.LOC_Y3, Config.LOC_Z3, Rnd.get(1, 20)));
		_walkNodes.add(new WalkNode(Config.LOC_X4, Config.LOC_Y4, Config.LOC_Z4, Rnd.get(1, 20)));
		_walkNodes.add(new WalkNode(Config.LOC_X5, Config.LOC_Y5, Config.LOC_Z5, Rnd.get(1, 20)));		
		_walkNodes.add(new WalkNode(Config.LOC_X6, Config.LOC_Y6, Config.LOC_Z6, Rnd.get(1, 20)));
		_walkNodes.add(new WalkNode(Config.LOC_X7, Config.LOC_Y7, Config.LOC_Z7, Rnd.get(1, 20)));
		_walkNodes.add(new WalkNode(Config.LOC_X8, Config.LOC_Y8, Config.LOC_Z8, Rnd.get(1, 20)));
		_walkNodes.add(new WalkNode(Config.LOC_X9, Config.LOC_Y9, Config.LOC_Z9, Rnd.get(1, 20)));
		_walkNodes.add(new WalkNode(Config.LOC_X10, Config.LOC_Y10, Config.LOC_Z10, Rnd.get(1, 20)));
		_walkNodes.add(new WalkNode(Config.LOC_X11, Config.LOC_Y11, Config.LOC_Z11, Rnd.get(1, 20)));
		_walkNodes.add(new WalkNode(Config.LOC_X12, Config.LOC_Y12, Config.LOC_Z12, Rnd.get(1, 20)));
		_walkNodes.add(new WalkNode(Config.LOC_X13, Config.LOC_Y13, Config.LOC_Z13, Rnd.get(1, 20)));
	}
}
