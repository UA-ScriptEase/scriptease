package scriptease.controller.get;

import scriptease.model.complex.AskIt;

public class AskItGetter extends TypeGetterVisitor<AskIt> {	
	@Override
	public void processAskIt( AskIt askIt ){
		this.objects.add( askIt );
	}

}
 