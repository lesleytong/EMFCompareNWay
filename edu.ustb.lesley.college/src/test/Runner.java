package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.ustb.sei.mde.bxcore.exceptions.NothingReturnedException;
import edu.ustb.sei.mde.bxcore.util.EcoreModelUtil;
import edu.ustb.sei.mde.bxcore.util.XmuProgram;
import edu.ustb.sei.mde.graph.type.PropertyEdge;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import edu.ustb.sei.mde.graph.typedGraph.BXMerge3;
import edu.ustb.sei.mde.graph.typedGraph.TypedEdge;
import edu.ustb.sei.mde.graph.typedGraph.TypedGraph;
import edu.ustb.sei.mde.graph.typedGraph.TypedNode;
import edu.ustb.sei.mde.graph.typedGraph.ValueEdge;
import my.MatchN;

public class Runner extends XmuProgram {

	public void testMerge(ArrayList<Resource> resources, List<MatchN> matches) {

		final String metamodelPath = "E:/git/n-way/edu.ustb.lesley.college/model/college.ecore";
		registerCollegePackage(URI.createFileURI(metamodelPath));
		
		final String mergeModelPath = "E:/git/n-way/edu.ustb.lesley.college/src/test/merge.xmi";

		Map<EObject, TypedNode> typedNodeMap = new HashMap<>(); // 方便mergeIndex
		Map<Resource, TypedGraph> typedGraphMap = new HashMap<>(); // 方便reindexing

		Table<TypedNode, PropertyEdge, ValueEdge> valueEdgeTable = HashBasedTable.create(); // 方便ValueEdge
		Table<EObject, EReference, TypedEdge> typedEdgeTable = HashBasedTable.create(); // 方便TypedEdge

		Resource baseResource = resources.get(0);
		TypedGraph baseGraph = loadCollegeModel(baseResource, typedNodeMap, valueEdgeTable, typedEdgeTable);
		typedGraphMap.put(baseResource, baseGraph);
		print(baseGraph);

		ArrayList<TypedGraph> branchGraphs = new ArrayList<>();
		for (int i = 1; i < resources.size(); i++) {
			Resource branchResource = resources.get(i);
			TypedGraph branchGraph = loadCollegeModel(branchResource, typedNodeMap, valueEdgeTable, typedEdgeTable);
			typedGraphMap.put(branchResource, branchGraph);
			branchGraphs.add(branchGraph);
			print(branchGraph);
		}

		for (MatchN m : matches) {
			EObject baseEObject = m.getBase();
			EList<EObject> branchesEObject = m.getBranches();

			if (baseEObject != null) {
				TypedNode baseNode = typedNodeMap.get(baseEObject);
				// TypedNode
				branchesEObject.forEach(b -> {
					TypedNode branchNode = typedNodeMap.get(b);
					branchNode.mergeIndex(baseNode);
					// 分支图调用reindexing
					TypedGraph branchGraph = typedGraphMap.get(b.eResource());
					branchGraph.reindexing(branchNode);
				});

				// ValueEdge
				List<PropertyEdge> allPropertyEdges = baseGraph.getTypeGraph().getAllPropertyEdges(baseNode.getType());
				allPropertyEdges.forEach(propertyEdge -> {
					ValueEdge baseValueEdge = valueEdgeTable.get(baseNode, propertyEdge);
					branchesEObject.forEach(b -> {
						TypedNode branchNode = typedNodeMap.get(b);
						ValueEdge branchValueEdge = valueEdgeTable.get(branchNode, propertyEdge);
						branchValueEdge.mergeIndex(baseValueEdge);
						TypedGraph branchGraph = typedGraphMap.get(b.eResource());
						branchGraph.reindexing(branchValueEdge);

					});

				});

				// TypedEdge
				EReference baseRef = baseEObject.eContainmentFeature();
				if (baseRef != null) {
					TypedEdge baseTypedEdge = typedEdgeTable.get(baseEObject, baseRef);
					branchesEObject.forEach(b -> {
						EStructuralFeature branchRef = b.eContainingFeature();
						if (branchRef != null) {
							TypedEdge branchTypedEdge = typedEdgeTable.get(b, branchRef);
							branchTypedEdge.mergeIndex(baseTypedEdge);
							TypedGraph branchGraph = typedGraphMap.get(b.eResource());
							branchGraph.reindexing(branchTypedEdge);
						}
					});
				}

			} else if (branchesEObject.size() > 0) {
				EObject branchFirstEObject = branchesEObject.get(0);
				TypedNode branchFirstNode = typedNodeMap.get(branchFirstEObject);

				for (int i = 1; i < branchesEObject.size(); i++) {
					EObject branchEObject = branchesEObject.get(i);
					TypedNode branchNode = typedNodeMap.get(branchEObject);
					branchNode.mergeIndex(branchFirstNode);
					// 分支图调用reindexing
					TypedGraph branchGraph = typedGraphMap.get(branchEObject.eResource());
					branchGraph.reindexing(branchNode);
				}

				// ValueEdge
				List<PropertyEdge> allPropertyEdges = baseGraph.getTypeGraph()
						.getAllPropertyEdges(branchFirstNode.getType());
				allPropertyEdges.forEach(propertyEdge -> {
					ValueEdge branchFirstValueEdge = valueEdgeTable.get(branchFirstNode, propertyEdge);
					for (int i = 1; i < branchesEObject.size(); i++) {
						EObject branchEObject = branchesEObject.get(i);
						TypedNode branchNode = typedNodeMap.get(branchEObject);
						ValueEdge branchValueEdge = valueEdgeTable.get(branchNode, propertyEdge);
						branchValueEdge.mergeIndex(branchFirstValueEdge);
						TypedGraph branchGraph = typedGraphMap.get(branchEObject.eResource());
						branchGraph.reindexing(branchValueEdge);
					}
				});

				// TypedEdge
				EReference branchFirstRef = branchFirstEObject.eContainmentFeature();
				TypedEdge branchFirstTypedEdge = typedEdgeTable.get(branchFirstEObject, branchFirstRef);
				if (branchFirstRef != null) {
					for (int i = 1; i < branchesEObject.size(); i++) {
						EObject branchEObject = branchesEObject.get(i);
						EReference branchRef = branchEObject.eContainmentFeature();
						if (branchRef != null) {
							TypedEdge branchTypedEdge = typedEdgeTable.get(branchEObject, branchRef);
							branchTypedEdge.mergeIndex(branchFirstTypedEdge);
							TypedGraph branchGraph = typedGraphMap.get(branchEObject.eResource());
							branchGraph.reindexing(branchTypedEdge);
						}

					}
				}

			}

		}

//		// tmp: 测试一下TypedNode是否合并了索引集 - 成功
//		TypeNode[] nodeImages = new TypeNode[branchGraphs.size()];
//		TypedNode baseNode = baseGraph.getAllTypedNodes().get(0);
//		for (int i = 0; i < branchGraphs.size(); i++) {
//			// 多个分支图先分别和基本图作比较，baseNode的情况分别存储在nodeImages[i]中。可能是NULL、ANY、修改后的类型
//			// computeImage可见性暂时改为public 
//			nodeImages[i] = TypedGraph.computeImage(baseNode, baseGraph, branchGraphs.get(i));
//		}

//		// tmp: 测试一下ValueEdge是否合并了索引集 - 成功
//		ValueEdge[] valueEdgeImages = new ValueEdge[branchGraphs.size()];
//		ValueEdge baseEdge = baseGraph.getAllValueEdges().get(0);
//		for (int i = 0; i < branchGraphs.size(); i++) {
//			valueEdgeImages[i] = TypedGraph.computeImage(baseEdge, baseGraph, branchGraphs.get(i));
//		}

		// tmp: 测试一下TypedEdge是否合并了索引集 - 成功
//		TypedEdge[] typedEdgeImages = new TypedEdge[branchGraphs.size()];
//		TypedEdge baseEdge = baseGraph.getAllTypedEdges().get(0);
//		for (int i = 0; i < branchGraphs.size(); i++) {
//			typedEdgeImages[i] = TypedGraph.computeImage(baseEdge, baseGraph, branchGraphs.get(i));
//		}

		TypedGraph[] branchGraphsArray = (TypedGraph[]) branchGraphs.toArray(new TypedGraph[branchGraphs.size()]);

		try {
			TypedGraph result = BXMerge3.merge(baseGraph, branchGraphsArray);
			System.out.println("**************************************result: ");
			print(result);
			
			saveSabModel(URI.createFileURI(mergeModelPath), result);
			
		} catch (NothingReturnedException e) {
			e.printStackTrace();
		}

	}
	
	public void saveSabModel(final URI uri, final TypedGraph graph) throws NothingReturnedException{
		EcoreModelUtil.save(uri, graph, null, getPackage("edu.ustb.lesley.college"));
	}

	public void registerCollegePackage(final URI metamodelUri) {
		registerPackage("edu.ustb.lesley.college", metamodelUri);
	}

	public TypedGraph loadCollegeModel(Resource r, Map<EObject, TypedNode> typedNodeMap,
			Table<TypedNode, PropertyEdge, ValueEdge> valueEdgeTable,
			Table<EObject, EReference, TypedEdge> typedEdgeTable) {
		List<EObject> roots = r.getContents();
		TypedGraph graph = EcoreModelUtilN.load(roots, getCollegeTypeGraph(), typedNodeMap, valueEdgeTable,
				typedEdgeTable);
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
