package testuml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;

import edu.ustb.sei.mde.bxcore.util.EcoreModelUtil;
import edu.ustb.sei.mde.graph.type.TypeEdge;
import edu.ustb.sei.mde.graph.type.TypeGraph;

public class TestBank {

	public static void main(String[] args) {

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION,
				UMLResource.Factory.INSTANCE);

		EPackage ep = UMLPackage.eINSTANCE;

		URI baseURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\bank.uml");
		URI backupURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\bank_backup.uml");
		URI m0URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\bank_m0.uml");
		URI m1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\bank_m1.uml");
		URI m2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\bank_m2.uml");

		Resource baseResource = resourceSet.getResource(baseURI, true);

		URI branch1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\bank1.uml");
		URI branch2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\bank2.uml");
		URI branch3URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\bank3.uml");

		List<URI> uriList = new ArrayList<>();
		uriList.add(baseURI);
		uriList.add(branch1URI);
		uriList.add(branch2URI);
		uriList.add(branch3URI);

		TypeGraph typeGraph = EcoreModelUtil.load(ep);
				
		// 指定需要排序的边类型(TypeEdge)
		List<TypeEdge> typeEdgeList = new ArrayList<>();
		TypeEdge typeEdge1 = typeGraph.getTypeEdge(typeGraph.getTypeNode("Class"), "ownedAttribute");
		TypeEdge typeEdge2 = typeGraph.getTypeEdge(typeGraph.getTypeNode("Class"), "ownedOperation");
		TypeEdge typeEdge3 = typeGraph.getTypeEdge(typeGraph.getTypeNode("Package"), "packagedElement");
		typeEdgeList.add(typeEdge1);
		typeEdgeList.add(typeEdge2);
//		typeEdgeList.add(typeEdge3);

//		EvaluateEngine.getM0(baseResource, m0URI, 1);			
//		EvaluateEngine.getBranches(baseResource, resourceSet, backupURI, m0URI, uriList);

//		EvaluateEngine.testMerge(typeGraph, resourceSet, uriList, typeEdgeList, null, m1URI, ep);
//		EvaluateEngine.testEquality(resourceSet, m0URI, m1URI);

//		EvaluateEngine.testEMFCompare(resourceSet, uriList, m2URI);
//		EvaluateEngine.testEquality(resourceSet, m0URI, m2URI);

	}

}
