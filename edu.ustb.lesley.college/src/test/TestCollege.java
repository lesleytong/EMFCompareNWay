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
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import college.CollegePackage;
import edu.ustb.sei.mde.bxcore.exceptions.NothingReturnedException;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import edu.ustb.sei.mde.graph.typedGraph.TypedGraph;
import my.MatchN;
import nway.NWay;

public class TestCollege {

	static String NsURIName = "https://edu/ustb/lesley/college";
	static EPackageImpl ep = (EPackageImpl) CollegePackage.eINSTANCE;

	static URI baseURI = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/college.xmi");
	static URI m0URI = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/college_m0.xmi");

	static URI branch1URI = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/college1.xmi");
	static URI branch2URI = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/college2.xmi");
	static URI branch3URI = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/college3.xmi");

	static URI metaModelURI = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/model/college.ecore");
	static URI m1URI = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/college_m1.xmi");

	public static void main(String[] args) {

//		getM0();
//		getBranches();
//		testMerge();
		testEquality();
	}

	public static void getM0() {

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(NsURIName, ep);
		Resource resource = resourceSet.getResource(baseURI, true);

		ChangeToolForCollege changeToolForCollege = new ChangeToolForCollege();

		// resource, start, changeCount, deleteCount
		changeToolForCollege.change(resource, 1, 2, 0);
		// addCount, addCountName
		changeToolForCollege.add(2, "Bob");

		// resource, start, changeCount, deleteCount
		changeToolForCollege.change(resource, 5, 0, 1);
		// addCount, addCountName
		changeToolForCollege.add(1, "Candy");
		
		// resource, start, changeCount, deleteCount
		changeToolForCollege.change(resource, 7, 1, 0);
		// addCount, addCountName
		changeToolForCollege.add(1, "Tina");
		
		changeToolForCollege.save(m0URI);

		System.out.println("down");
	}

	public static void getBranches() {

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

		ResourceSet resourceSet1 = new ResourceSetImpl();
		resourceSet1.getPackageRegistry().put(NsURIName, ep);
		Resource resource1 = resourceSet1.getResource(baseURI, true);

		ResourceSet resourceSet2 = new ResourceSetImpl();
		resourceSet2.getPackageRegistry().put(NsURIName, ep);
		Resource resource2 = resourceSet2.getResource(baseURI, true);

		ResourceSet resourceSet3 = new ResourceSetImpl();
		resourceSet3.getPackageRegistry().put(NsURIName, ep);
		Resource resource3 = resourceSet3.getResource(baseURI, true);

		ChangeToolForCollege changeToolForCollege = new ChangeToolForCollege();
		
		// resource, start, changeCount, deleteCount
		changeToolForCollege.change(resource1, 1, 2, 0);
		// addCount, addCountName
		changeToolForCollege.add(2, "Bob");
		changeToolForCollege.save(branch1URI);
		
		// resource, start, changeCount, deleteCount
		changeToolForCollege.change(resource2, 5, 0, 1);
		// addCount, addCountName
		changeToolForCollege.add(2, "Bob");
		changeToolForCollege.add(1, "Candy");
		changeToolForCollege.save(branch2URI);
		
		// resource, start, changeCount, deleteCount
		changeToolForCollege.change(resource3, 7, 1, 0);
		// addCount, addCountName
		changeToolForCollege.add(2, "Bob");
		changeToolForCollege.add(1, "Candy");
		changeToolForCollege.add(1, "Tina");
		changeToolForCollege.save(branch3URI);

		System.out.println("down");
	}

	public static void testMerge() {

		ArrayList<URI> uriList = new ArrayList<>();
		uriList.add(baseURI);
		uriList.add(branch1URI);
		uriList.add(branch2URI);
		uriList.add(branch3URI);

		TypeGraph typeGraph = getCollegeTypeGraph();

		long start = System.currentTimeMillis();
		NWay nWay = new NWay(NsURIName, ep, typeGraph);
		List<MatchN> matches = nWay.nMatch(uriList);
		TypedGraph mergeModel = nWay.nMerge(matches, "");
		long end = System.currentTimeMillis();
		System.out.println("总耗时：" + (end - start) + "ms.");
		System.out.println("****************************************************");

		try {
			nWay.saveModel(metaModelURI, m1URI, mergeModel);
		} catch (NothingReturnedException e) {
			e.printStackTrace();
		}

//		// 调用EMF Compare的三路比较
//		TWayCompare tWayCompare = new TWayCompare(NsURIName, ep);
//		tWayCompare.tWay(uriBase, uriBranch1, uriBranch2);
//		System.out.println("down");
	}

	// 比较M0和M1
	public static void testEquality() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(NsURIName, ep);

		Resource leftResource = resourceSet.getResource(m1URI, true);
		Resource rightResource = resourceSet.getResource(m0URI, true);

		IComparisonScope scope = new DefaultComparisonScope(leftResource, rightResource, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);

		EList<Diff> diffs = comparison.getDifferences();
		System.out.println("diffs: " + diffs.size());
		diffs.forEach(d -> {
			System.out.println(d);
		});

	}

	public static TypeGraph getCollegeTypeGraph() {
		TypeGraph typeGraph = new TypeGraph();
		// TypedNode
		typeGraph.declare("AddressBook");
		typeGraph.declare("Person");
		typeGraph.declare("College");
		// ValueNode
		typeGraph.declare("EString:java.lang.String");
		typeGraph.declare("EInt:java.lang.Integer");
		// ValueEdge
		typeGraph.declare("name:AddressBook->EString");
		typeGraph.declare("name:Person->EString");
		typeGraph.declare("age:Person->EInt");
		typeGraph.declare("name:College->EString");
		// TypedEdge
		typeGraph.declare("@persons:AddressBook->Person*");
		typeGraph.declare("@college:Person->College*");
		typeGraph.declare("@friends:Person->Person*");
		return typeGraph;
	}

}
