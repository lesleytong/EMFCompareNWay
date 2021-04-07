package test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import edu.ustb.sei.mde.bxcore.exceptions.NothingReturnedException;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import edu.ustb.sei.mde.graph.typedGraph.TypedGraph;
import my.MatchN;
import nway.ChangeTool;
import nway.EcoreTypeGraph;
import nway.NWay;

public class TestPurchase {
	
	static URI baseURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase.xmi");
	static URI m0URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase_m0.xmi");
	
	static URI branch1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase_out1.xmi");
	static URI branch2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase_out2.xmi");
	static URI branch3URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase_out3.xmi");
	
	static URI metaModelURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.compare\\model\\Ecore.ecore");
	static URI m1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase_m1.xmi");
	

	public static void main(String[] args) {

//		getM0();
//		getBranches();
		testMerge();
//		testEquality();

	}
	
	public static void getM0() {
				
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.getResource(baseURI, true);
		
		ChangeTool changeTool = new ChangeTool();
		
		// start, changeCount, deleteCount, addCount, addCountName
		changeTool.set(1, 1, 0, 2, "NewClass");
		changeTool.change(resource);
		
		changeTool.set(5, 1, 0, 2, "Hello");
		changeTool.change(resource);
		
		changeTool.set(6, 0, 1, 1, "Moon");
		changeTool.change(resource);
		changeTool.save(m0URI);

		System.out.println("down");
		
	}
	
	public static void getBranches() {
		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		
		ResourceSet resourceSet1 = new ResourceSetImpl();
		Resource resource1 = resourceSet1.getResource(baseURI, true);

		ResourceSet resourceSet2 = new ResourceSetImpl();
		Resource resource2 = resourceSet2.getResource(baseURI, true);

		ResourceSet resourceSet3 = new ResourceSetImpl();
		Resource resource3 = resourceSet3.getResource(baseURI, true);

		ChangeTool changeTool = new ChangeTool();
		
		// start, changeCount, deleteCount, addCount, addCountName
		changeTool.set(1, 1, 0, 2, "NewClass");
		changeTool.change(resource1);
		changeTool.save(branch1URI);

		changeTool.set(5, 1, 0, 2, "Hello");
		changeTool.change(resource2);
		changeTool.save(branch2URI);

		changeTool.set(6, 0, 1, 1, "Moon");
		changeTool.change(resource3);
		changeTool.save(branch3URI);

		System.out.println("down");
	}
	
	public static void testMerge() {

		ArrayList<URI> uriList = new ArrayList<>();
		uriList.add(baseURI);
		uriList.add(branch1URI);
		uriList.add(branch2URI);
		uriList.add(branch3URI);

		EcoreTypeGraph et = new EcoreTypeGraph();
		TypeGraph typeGraph = et.getTypeGraph_Ecore();

		long start = System.currentTimeMillis();
		NWay nWay = new NWay(typeGraph);
		List<MatchN> matches = nWay.nMatch(uriList);		
		TypedGraph mergeModel = nWay.nMerge(matches, "EClass-*->EStructuralFeature");
		long end = System.currentTimeMillis();
		System.out.println("总耗时： " + (end - start) + " ms.");		
		
		try {
			nWay.saveModel(metaModelURI, m1URI, mergeModel);
			System.out.println("down");
		} catch (NothingReturnedException e) {
			e.printStackTrace();
		}
	}
	
	// 比较M0和M1
	public static void testEquality() {		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource leftResource = resourceSet.getResource(m1URI, true);
		Resource rightResource = resourceSet.getResource(m0URI, true);
		
		IComparisonScope scope = new DefaultComparisonScope(leftResource, rightResource, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		
		EList<Diff> diffs = comparison.getDifferences();
		System.out.println("diffs: " + diffs.size());
		
	}

}
