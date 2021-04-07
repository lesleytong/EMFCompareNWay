package test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import college.CollegePackage;
import edu.ustb.sei.mde.bxcore.exceptions.NothingReturnedException;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import edu.ustb.sei.mde.graph.typedGraph.TypedGraph;
import my.MatchN;
import nway.NWay;

public class TestCollege {
	
	public static void main(String[] args) {
		
		String NsURIName = "https://edu/ustb/lesley/college";
		EPackageImpl ep = (EPackageImpl) CollegePackage.eINSTANCE;

		URI uriBase = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/college.xmi");
		URI uriBranch1 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/college1.xmi");
		URI uriBranch2 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/college2.xmi");
		URI uriBranch3 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/college3.xmi");

		ArrayList<URI> uriList = new ArrayList<>();
		uriList.add(uriBase);
		uriList.add(uriBranch1);
		uriList.add(uriBranch2);
		uriList.add(uriBranch3);

		TypeGraph typeGraph = getCollegeTypeGraph();

		URI metaModelURI = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/model/college.ecore");
		URI m1URI = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/college_merge.xmi");
		
		long start = System.currentTimeMillis();
		NWay nWay = new NWay(NsURIName, ep, typeGraph);
		List<MatchN> matches = nWay.nMatch(uriList);
		TypedGraph mergeModel = nWay.nMerge(matches, "");
		long end = System.currentTimeMillis();
		System.out.println("总耗时：" + (end-start) + "ms.");
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
