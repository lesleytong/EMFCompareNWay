package testecore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import edu.ustb.sei.mde.graph.type.TypeEdge;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import nway.EcoreTypeGraph;
import nway.EvaluateEngine;

public class TestBankingSystem {

	public static void main(String[] args) {
		

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		
		EPackage ep = EcorePackage.eINSTANCE;
		
		URI baseURI =  URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testecore\\bankingSystem.ecore");
		URI backupURI =  URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testecore\\bankingSystem_backup.ecore");
		URI m0URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testecore\\bankingSystem_m0.ecore"); 
		URI m1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testecore\\bankingSystem_m1.ecore"); 
		URI m2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testecore\\bankingSystem_m2.ecore"); 
		
		Resource baseResource = resourceSet.getResource(baseURI, true);

		URI branch1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testecore\\bankingSystem1.ecore");
		URI branch2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testecore\\bankingSystem2.ecore");
		URI branch3URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testecore\\bankingSystem3.ecore");

		List<URI> uriList = new ArrayList<>();
		uriList.add(baseURI);
		uriList.add(branch1URI);
		uriList.add(branch2URI);
		uriList.add(branch3URI);
		
		EcoreTypeGraph et = new EcoreTypeGraph();
		TypeGraph typeGraph = et.getTypeGraph_Ecore();
		
		// 指定需要排序的边类型(TypeEdge)
		List<TypeEdge> typeEdgeList = new ArrayList<>();
		TypeEdge typeEdge1 = typeGraph.getTypeEdge(typeGraph.getTypeNode("EClass"), "eStructuralFeatures");
		TypeEdge typeEdge2 = typeGraph.getTypeEdge(typeGraph.getTypeNode("EClass"), "eOperations");
		typeEdgeList.add(typeEdge1);
		typeEdgeList.add(typeEdge2);
		
		// 指定需要过滤的导出引用
		Map<String, Boolean> filterDiffMap = new HashMap<>();
		filterDiffMap.put("eSuperTypes", true);
		filterDiffMap.put("instanceClassName", true);

		EvaluateEngine.getM0(baseResource, m0URI, 2);			
//		EvaluateEngine.getBranches(baseResource, resourceSet, backupURI, m0URI, uriList);

//		EvaluateEngine.testMerge(typeGraph, resourceSet, uriList, typeEdgeList, null, m1URI, ep);
//		EvaluateEngine.testEquality(resourceSet, m0URI, m1URI, filterDiffMap);

//		EvaluateEngine.testEMFCompare(resourceSet, uriList, m2URI);
//		EvaluateEngine.testEquality(resourceSet, m0URI, m2URI);

	}

}
