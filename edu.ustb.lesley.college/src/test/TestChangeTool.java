package test;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import nway.ChangeTool;

public class TestChangeTool {

	public static void main(String[] args) {
		
		URI uriBranch1 = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase_change.xmi");
			
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		
		Resource branchResource1 = resourceSet.getResource(uriBranch1, true);
		
		ChangeTool.change(branchResource1);
		
	}

}
