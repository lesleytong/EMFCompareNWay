package test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.junit.Test;

import college.CollegePackage;
import edu.ustb.sei.mde.bxcore.exceptions.NothingReturnedException;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import edu.ustb.sei.mde.graph.typedGraph.TypedGraph;
import my.MatchN;
import nway.NWay;
import nway.ThreeWayCompare;

public class TestCollege {

	@Test
	public void testMerge() {

		String NsURI = "https://edu/ustb/lesley/college";
		EPackageImpl packageImpl = (EPackageImpl) CollegePackage.eINSTANCE;

		URI uriBase = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add.xmi");
		URI uriBranch1 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add1.xmi");
		URI uriBranch2 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add2.xmi");
		URI uriBranch3 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add3.xmi");

		ArrayList<URI> uriList = new ArrayList<>();
		uriList.add(uriBase);
		uriList.add(uriBranch1);
		uriList.add(uriBranch2);
//		uriList.add(uriBranch3);

		TypeGraph typeGraph = getCollegeTypeGraph();

		String metaModelPath = "E:/git/n-way/edu.ustb.lesley.college/model/college.ecore";
		String mergeModelPath = "E:/git/n-way/edu.ustb.lesley.college/src/test/merge.xmi";

		NWay nWay = new NWay(NsURI, packageImpl, uriList, typeGraph, metaModelPath, mergeModelPath);
		long start = System.currentTimeMillis();
		List<MatchN> matches = nWay.nMatch();
		long end = System.currentTimeMillis();
		System.out.println("match cost time of NWay: " + (end - start) + " ms.");

		start = System.currentTimeMillis();
		TypedGraph mergeModel = nWay.nMerge(matches);
		end = System.currentTimeMillis();
		System.out.println("merge cost time of NWay: " + (end - start) + " ms.");

		System.out.println("************************************merge model");
		nWay.print(mergeModel);
		try {
			nWay.saveModel(URI.createFileURI(mergeModelPath), mergeModel);
		} catch (NothingReturnedException e) {
			e.printStackTrace();
		}

		ThreeWayCompare t = new ThreeWayCompare(NsURI, packageImpl, uriBase, uriBranch1, uriBranch2);
		start = System.currentTimeMillis();
		t.threeWay();
		end = System.currentTimeMillis();
		System.out.println("the whole cost time of EMF Compare: " + (end - start) + " ms");

	}

	public TypeGraph getCollegeTypeGraph() {
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
