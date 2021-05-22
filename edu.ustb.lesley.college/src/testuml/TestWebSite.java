package testuml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import edu.ustb.sei.mde.graph.type.TypeEdge;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import nway.ChangeEngine;
import nway.EcoreTypeGraph;

public class TestWebSite {

	public static void main(String[] args) {
		

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		
		String NsURIName = EcorePackage.eNS_URI;
		
		URI metaModelURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.compare\\model\\Ecore.ecore");
		URI baseURI =  URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\website.ecore");
		URI backupURI =  URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\website_backup.ecore");
		URI m0URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\website_m0.ecore"); 
		URI m1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\website_m1.ecore"); 
		URI m2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\website_m2.ecore"); 
		
		Resource baseResource = resourceSet.getResource(baseURI, true);

		URI branch1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\website1.ecore");
		URI branch2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\website2.ecore");
		URI branch3URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testuml\\website3.ecore");

		List<URI> uriList = new ArrayList<>();
		uriList.add(baseURI);
		uriList.add(branch1URI);
		uriList.add(branch2URI);
		uriList.add(branch3URI);
		
		EcoreTypeGraph et = new EcoreTypeGraph();
		TypeGraph typeGraph = et.getTypeGraph_Ecore();
		
		// typeEdgeList指定需要进行排序的边类型
		TypeEdge typeEdge1 = typeGraph.getTypeEdge(typeGraph.getTypeNode("EClass"), "eStructuralFeatures");
		TypeEdge typeEdge2 = typeGraph.getTypeEdge(typeGraph.getTypeNode("EClass"), "eOperations");
		List<TypeEdge> typeEdgeList = new ArrayList<>();
		typeEdgeList.add(typeEdge1);
		typeEdgeList.add(typeEdge2);

//		ChangeEngine.getM0(baseResource, m0URI, false);			
//		ChangeEngine.getBranches(baseResource, resourceSet, backupURI, m0URI, uriList);

//		ChangeEngine.testMerge(typeGraph, resourceSet, uriList, typeEdgeList, null, NsURIName, metaModelURI, m1URI);
//		ChangeEngine.testEquality(resourceSet, m0URI, m1URI);

//		ChangeEngine.testEMFCompare(resourceSet, uriList, m2URI);
//		ChangeEngine.testEquality(resourceSet, m0URI, m2URI);

	}

}
