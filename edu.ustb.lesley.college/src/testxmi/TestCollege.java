package testxmi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import college.CollegePackage;
import edu.ustb.sei.mde.bxcore.util.EcoreModelUtil;
import edu.ustb.sei.mde.graph.type.PropertyEdge;
import edu.ustb.sei.mde.graph.type.TypeEdge;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import nway.EvaluateEngine;

public class TestCollege {

	public static void main(String[] args) {

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

		EPackage ep = CollegePackage.eINSTANCE;

		URI baseURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testxmi\\college.xmi");
		Resource baseResource = resourceSet.getResource(baseURI, true);

		URI backupURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testxmi\\college_backup.xmi");
		URI m0URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testxmi\\college_m0.xmi");
		URI m1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testxmi\\college_m1.xmi");
		URI m2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testxmi\\college_m2.xmi");

		URI branch1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testxmi\\college1.xmi");
		URI branch2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testxmi\\college2.xmi");
		URI branch3URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\testxmi\\college3.xmi");

		List<URI> uriList = new ArrayList<>();
		uriList.add(baseURI);
		uriList.add(branch1URI);
		uriList.add(branch2URI);
		uriList.add(branch3URI);

		TypeGraph typeGraph = EcoreModelUtil.load(ep);

		// 指定需要排序的边类型(TypeEdge)
		List<TypeEdge> typeEdgeList = new ArrayList<>();
		typeEdgeList.addAll(typeGraph.getAllTypeEdges());
		// 指定需要排序的边类型(PropertyEdge)
		List<PropertyEdge> propertyEdgeList = new ArrayList<>();
		PropertyEdge propertyEdge = typeGraph.getPropertyEdge(typeGraph.getTypeNode("College"), "title");
		propertyEdgeList.add(propertyEdge);

//		EvaluateEngine.getM0(baseResource, m0URI, 3);			
		EvaluateEngine.getBranches(baseResource, resourceSet, backupURI, m0URI, uriList);

//		EvaluateEngine.testMerge(typeGraph, resourceSet, uriList, typeEdgeList, propertyEdgeList, m1URI, ep);
//		EvaluateEngine.testEquality(resourceSet, m0URI, m1URI);

//		EvaluateEngine.testEMFCompare(resourceSet, uriList, m2URI);
//		EvaluateEngine.testEquality(resourceSet, m0URI, m2URI);

	}

}
