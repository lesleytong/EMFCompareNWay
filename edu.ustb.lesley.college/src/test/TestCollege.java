package test;

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
import nway.ChangeEngine;

public class TestCollege {

	public static void main(String[] args) {

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		
		String NsURIName = "https://edu/ustb/lesley/college";
		EPackage ep = CollegePackage.eINSTANCE;
		resourceSet.getPackageRegistry().put(NsURIName, ep);
		
		URI baseURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college.xmi");
		Resource baseResource = resourceSet.getResource(baseURI, true);

		URI metaModelURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\model\\college.ecore");
		URI backupURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college_backup.xmi");
		URI m0URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college_m0.xmi");
		URI m1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college_m1.xmi");
		URI m2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college_m2.xmi");


		URI branch1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college1.xmi");
		URI branch2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college2.xmi");
		URI branch3URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college3.xmi");

		List<URI> uriList = new ArrayList<>();
		uriList.add(baseURI);
		uriList.add(branch1URI);
		uriList.add(branch2URI);
		uriList.add(branch3URI);

		TypeGraph typeGraph = EcoreModelUtil.load(ep);
		
		// 指定了需要排序的边类型TypeEdge
		List<TypeEdge> typeEdgeList = typeGraph.getAllTypeEdges();
		// 指定了需要排序的边类型ValueEdge
		PropertyEdge propertyEdge = typeGraph.getPropertyEdge(typeGraph.getTypeNode("College"), "title");
		List<PropertyEdge> propertyEdgeList = new ArrayList<>();
		propertyEdgeList.add(propertyEdge);

//		ChangeEngine.getM0(baseResource, m0URI, true);			
//		ChangeEngine.getBranches(baseResource, resourceSet, backupURI, m0URI, uriList);

//		ChangeEngine.testMerge(typeGraph, resourceSet, uriList, typeEdgeList, propertyEdgeList, NsURIName, metaModelURI, m1URI);
//		ChangeEngine.testEquality(resourceSet, m0URI, m1URI);

//		ChangeEngine.testEMFCompare(resourceSet, uriList, m2URI);
//		ChangeEngine.testEquality(resourceSet, m0URI, m2URI);

	}

}
