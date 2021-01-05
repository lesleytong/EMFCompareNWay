package test;

import java.util.ArrayList;

import org.eclipse.emf.common.util.URI;
import org.junit.Test;

import edu.ustb.sei.mde.bxcore.util.XmuProgram;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import edu.ustb.sei.mde.graph.typedGraph.TypedGraph;

public class Runner extends XmuProgram {

	@Test
	public void testMerge() {

		URI uriBase = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add.xmi");
		URI uriBranch1 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add1.xmi");
		URI uriBranch2 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add2.xmi");
		URI uriBranch3 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add3.xmi");
		URI uriBranch4 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add4.xmi");

		final String metamodelPath = "E:/git/n-way/edu.ustb.lesley.college/model/college.ecore";
		registerCollegePackage(URI.createFileURI(metamodelPath));

		TypedGraph baseModel = loadCollegeModel(uriBase);
		print(baseModel);

	}

	public void registerCollegePackage(final URI metamodelUri) {
		registerPackage("edu.ustb.lesley.college", metamodelUri);
	}

	public TypedGraph loadCollegeModel(final URI modelUri) {
		java.util.List<org.eclipse.emf.ecore.EObject> roots = edu.ustb.sei.mde.bxcore.util.EcoreModelUtil
				.load(modelUri);
		edu.ustb.sei.mde.graph.typedGraph.TypedGraph graph = edu.ustb.sei.mde.bxcore.util.EcoreModelUtil.load(roots,
				getCollegeTypeGraph());
		return graph;
	}

	private TypeGraph typeGraph;

	public TypeGraph getCollegeTypeGraph() {
		if (typeGraph == null) {
			typeGraph = new edu.ustb.sei.mde.graph.type.TypeGraph();
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
		}
		return typeGraph;
	}

	private void print(TypedGraph typedGraph) {

		System.out.println("TypedNodes: " + typedGraph.getAllTypedNodes().toString());
		System.out.println("TypedEdges: " + typedGraph.getAllTypedEdges().toString());
		System.out.println("ValueNodes: " + typedGraph.getAllValueNodes().toString());
		System.out.println("ValueEdges: " + typedGraph.getAllValueEdges().toString());
		System.out.println("*********************************************************************");
		System.out.println();
	}
}
