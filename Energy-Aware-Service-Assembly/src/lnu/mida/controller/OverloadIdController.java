package lnu.mida.controller;

import peersim.core.*;
import lnu.mida.entity.GeneralNode;

public class OverloadIdController implements Control {

	public OverloadIdController(String prefix) {
    }

	@Override
	public boolean execute() {
	   GeneralNode.counterID=-1;
	   return false;
    }

}
