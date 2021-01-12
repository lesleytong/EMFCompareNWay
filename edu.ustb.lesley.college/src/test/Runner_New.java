package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import college.impl.AddressBookImpl;
import edu.ustb.sei.mde.bxcore.XmuCoreUtils;
import edu.ustb.sei.mde.bxcore.exceptions.NothingReturnedException;
import edu.ustb.sei.mde.bxcore.util.EcoreModelUtil;
import edu.ustb.sei.mde.bxcore.util.XmuProgram;
import edu.ustb.sei.mde.graph.type.DataTypeNode;
import edu.ustb.sei.mde.graph.type.PropertyEdge;
import edu.ustb.sei.mde.graph.type.TypeEdge;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import edu.ustb.sei.mde.graph.type.TypeNode;
import edu.ustb.sei.mde.graph.typedGraph.BXMerge3;
import edu.ustb.sei.mde.graph.typedGraph.TypedEdge;
import edu.ustb.sei.mde.graph.typedGraph.TypedGraph;
import edu.ustb.sei.mde.graph.typedGraph.TypedNode;
import edu.ustb.sei.mde.graph.typedGraph.ValueEdge;
import edu.ustb.sei.mde.graph.typedGraph.ValueNode;
import my.MatchN;

public class Runner_New extends XmuProgram {

	TypeGraph typeGraph = getCollegeTypeGraph();

	public void testMerge(ArrayList<Resource> resources, List<MatchN> matches) {

		final String metamodelPath = "E:/git/n-way/edu.ustb.lesley.college/model/college.ecore";
		registerCollegePackage(URI.createFileURI(metamodelPath));

		final String mergeModelPath = "E:/git/n-way/edu.ustb.lesley.college/src/test/merge.xmi";

		Map<Resource, TypedGraph> typedGraphMap = new HashMap<>();
		HashMap<EObject, TypedNode> typedNodeMap = new HashMap<>();

		resources.parallelStream().forEach(r -> {
			typedGraphMap.put(r, new TypedGraph(typeGraph));
		});

		// MatchN -> new TypedNode
		for (MatchN m : matches) {
			EObject base = m.getBase();

			EList<EObject> branches = m.getBranches();
			if (base != null) {
				// TypedNode
				EClass cls = base.eClass();
				TypeNode typeNode = typeGraph.getTypeNode(cls.getName());
				TypedNode baseNode = new TypedNode(typeNode);
				TypedGraph baseGraph = typedGraphMap.get(base.eResource());
				baseGraph.addTypedNode(baseNode);
				typedNodeMap.put(base, baseNode);

				// ValueEdge and ValueNode
				Table<PropertyEdge, ValueNode, ValueEdge> valueEdgeTable = HashBasedTable.create();
				Map<PropertyEdge, ValueEdge> valueEdgeMap = new HashMap<>();
				addValueEdgesBase(base, baseNode, baseGraph, valueEdgeTable, valueEdgeMap);

				for (EObject b : branches) {
					// TypedNode
					TypedGraph branchGraph = typedGraphMap.get(b.eResource());
					branchGraph.addTypedNode(baseNode);
					typedNodeMap.put(b, baseNode);

					// ValueEdge and ValueNode
					addValueEdges(b, baseNode, branchGraph, valueEdgeTable, valueEdgeMap);

				}

			} else { // without base
				// TypedNode
				EObject branchFirst = branches.get(0);
				TypeNode typeNode = typeGraph.getTypeNode(branchFirst.eClass().getName());
				TypedNode branchFirstNode = new TypedNode(typeNode);
				TypedGraph branchFirstGraph = typedGraphMap.get(branchFirst.eResource());
				branchFirstGraph.addTypedNode(branchFirstNode);
				typedNodeMap.put(branchFirst, branchFirstNode);

				// ValueEdge and ValueNode
				Table<PropertyEdge, ValueNode, ValueEdge> valueEdgeTable = HashBasedTable.create();
				Map<PropertyEdge, ValueEdge> valueEdgeMap = new HashMap<>();
				addValueEdgesBase(branchFirst, branchFirstNode, branchFirstGraph, valueEdgeTable, valueEdgeMap);

				branches.parallelStream().skip(1).forEach(b -> {
					// TypedNode
					TypedGraph branchGraph = typedGraphMap.get(b.eResource());
					branchGraph.addTypedNode(branchFirstNode);
					typedNodeMap.put(b, branchFirstNode);

					// ValueEdge and ValueNode
					addValueEdges(b, branchFirstNode, branchGraph, valueEdgeTable, valueEdgeMap);

				});

			}

		}

		// TypedEdge
		for (MatchN m : matches) {
			EObject base = m.getBase();
			EList<EObject> branches = m.getBranches();

			if (base != null) {

				TypedNode baseNode = typedNodeMap.get(base);
				TypedGraph baseGraph = typedGraphMap.get(base.eResource());
				// TypedEdge
				Table<TypedNode, TypedNode, TypedEdge> typedEdgeTable = HashBasedTable.create();
				addTypedEdgesBase(base, baseNode, baseGraph, typedNodeMap, typedEdgeTable);

				for (EObject b : branches) {
					TypedGraph branchGraph = typedGraphMap.get(b.eResource());

					// TypedEdge
					addTypedEdges(b, baseNode, branchGraph, typedNodeMap, typedEdgeTable);
				}

			} else {

				EObject branchFirst = branches.get(0);
				TypedNode branchFirstNode = typedNodeMap.get(branchFirst);
				TypedGraph branchFirstGraph = typedGraphMap.get(branchFirst.eResource());

				// TypedEdge
				Table<TypedNode, TypedNode, TypedEdge> typedEdgeTable = HashBasedTable.create();
				addTypedEdgesBase(branchFirst, branchFirstNode, branchFirstGraph, typedNodeMap, typedEdgeTable);

				// TypedEdge
				branches.parallelStream().skip(1).forEach(b -> {
					TypedGraph branchGraph = typedGraphMap.get(b.eResource());
					addTypedEdges(b, branchFirstNode, branchGraph, typedNodeMap, typedEdgeTable);
				});

			}
		}

		TypedGraph[] branchGraphs = new TypedGraph[resources.size() - 1];
		for (int i = 1; i < resources.size(); i++) {
			branchGraphs[i - 1] = typedGraphMap.get(resources.get(i));
		}

		try {
			TypedGraph baseGraph = typedGraphMap.get(resources.get(0));
			TypedGraph resultGraph = BXMerge3.merge(baseGraph, branchGraphs);
			System.out.println("**************************************result: ");
			print(resultGraph);

			HashMap<TypedEdge, TypedEdge> forceOrd = new HashMap<>();
			TypedGraph mergeModel = BXMerge3.threeOrder(baseGraph, resultGraph, forceOrd, branchGraphs);

			saveSabModel(URI.createFileURI(mergeModelPath), mergeModel);

		} catch (NothingReturnedException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	public void addTypedEdges(EObject b, TypedNode baseNode, TypedGraph branchGraph,
			HashMap<EObject, TypedNode> typedNodeMap, Table<TypedNode, TypedNode, TypedEdge> typedEdgeTable) {
		EClass cls = b.eClass();
		TypeNode typeNode = typeGraph.getTypeNode(cls.getName());

		cls.getEAllReferences().forEach(r -> {
			TypeEdge typeEdge = typeGraph.getTypeEdge(typeNode, r.getName());
			if (r.isMany()) { // multi-reference
				Collection<EObject> targets = (Collection<EObject>) b.eGet(r);
				targets.forEach(t -> {
					TypedNode targetNode = typedNodeMap.get(t);
					TypedEdge typedEdge = typedEdgeTable.get(baseNode, targetNode);
					if (typedEdge != null) {
						branchGraph.addTypedEdge(typedEdge);
					} else {	// 不视作被修改
						TypedEdge typedEdgeBranch = new TypedEdge(baseNode, targetNode, typeEdge);
						branchGraph.addTypedEdge(typedEdgeBranch);
					}
					if (r.isContainment()) {
						addTypedEdges(t, baseNode, branchGraph, typedNodeMap, typedEdgeTable);
					}

				});
			} else { // single-reference
				EObject t = (EObject) b.eGet(r);
				if (t != null) {
					TypedNode targetNode = typedNodeMap.get(t);
					TypedEdge typedEdge = typedEdgeTable.get(baseNode, targetNode);
					if (typedEdge != null) {
						branchGraph.addTypedEdge(typedEdge);
					} else {	// 也不视作被修改
						TypedEdge typedEdgeBranch = new TypedEdge(baseNode, targetNode, typeEdge);
						branchGraph.addTypedEdge(typedEdgeBranch);
					}
					if (r.isContainment()) {
						addTypedEdges(t, baseNode, branchGraph, typedNodeMap, typedEdgeTable);
					}
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void addTypedEdgesBase(EObject base, TypedNode baseNode, TypedGraph baseGraph,
			HashMap<EObject, TypedNode> typedNodeMap, Table<TypedNode, TypedNode, TypedEdge> typedEdgeTable) {
		EClass cls = base.eClass();
		TypeNode typeNode = typeGraph.getTypeNode(cls.getName());

		cls.getEAllReferences().forEach(r -> {
			TypeEdge typeEdge = typeGraph.getTypeEdge(typeNode, r.getName());
			if (r.isMany()) { // multi-reference
				Collection<EObject> targets = (Collection<EObject>) base.eGet(r);
				targets.forEach(t -> {
					TypedNode targetNode = typedNodeMap.get(t);
					if (targetNode == null) {
						XmuCoreUtils.log(Level.WARNING,
								"The target node is not registered. The loader will ignore this edge: " + r, null);
					} else {
						TypedEdge typedEdge = new TypedEdge(baseNode, targetNode, typeEdge);
						baseGraph.addTypedEdge(typedEdge);
						// record typedEdge
						typedEdgeTable.put(baseNode, targetNode, typedEdge);

						if (r.isContainment()) {
							addTypedEdgesBase(t, baseNode, baseGraph, typedNodeMap, typedEdgeTable);
						}
					}
				});
			} else { // single-reference
				EObject t = (EObject) base.eGet(r);
				if (t != null) {
					TypedNode targetNode = typedNodeMap.get(t);
					if (targetNode == null) {
						XmuCoreUtils.log(Level.WARNING,
								"The target node is not registered. The loader will ignore this edge: " + r, null);
					} else {
						TypedEdge typedEdge = new TypedEdge(baseNode, targetNode, typeEdge);
						baseGraph.addTypedEdge(typedEdge);
						// record typedEdge
						typedEdgeTable.put(baseNode, targetNode, typedEdge);

						if (r.isContainment()) {
							addTypedEdgesBase(t, baseNode, baseGraph, typedNodeMap, typedEdgeTable);
						}
					}
				}
			}
		});
	}

	public void addValueEdges(EObject b, TypedNode baseNode, TypedGraph branchGraph,
			Table<PropertyEdge, ValueNode, ValueEdge> valueEdgeTable, Map<PropertyEdge, ValueEdge> valueEdgeMap) {
		EClass cls = b.eClass();
		TypeNode typeNode = typeGraph.getTypeNode(cls.getName());

		cls.getEAllAttributes().forEach(a -> {
			DataTypeNode dataTypeNode = typeGraph.getDataTypeNode(a.getEAttributeType().getName());
			PropertyEdge propertyEdge = typeGraph.getPropertyEdge(typeNode, a.getName());

			if (a.isMany()) {
				@SuppressWarnings("unchecked")
				Collection<Object> values = (Collection<Object>) b.eGet(a);
				values.forEach(v -> {
					if (a.getEAttributeType() instanceof EEnum) {
						if (v instanceof Enumerator)
							v = ((Enumerator) v).getLiteral();
						else
							v = v.toString();
					}
					ValueNode vn = ValueNode.createConstantNode(v, dataTypeNode);
					ValueEdge valueEdge = valueEdgeTable.get(propertyEdge, vn);
					if (valueEdge != null) {
						branchGraph.addValueEdge(valueEdge);
					} else { // 多值属性不看作被修改
						branchGraph.addValueNode(vn);
						ValueEdge branchValueEdge = new ValueEdge(baseNode, vn, propertyEdge);
						branchGraph.addValueEdge(branchValueEdge);
					}
				});
			} else { // single-valued
				Object v = b.eGet(a);
				if (v != null) {
					if (a.getEAttributeType() instanceof EEnum) {
						if (v instanceof Enumerator)
							v = ((Enumerator) v).getLiteral();
						else
							v = v.toString();
					}

					ValueEdge valueEdge = valueEdgeMap.get(propertyEdge);
					if (valueEdge.getTarget().getValue() == v) {
						branchGraph.addValueEdge(valueEdge);
					} else { // 单值属性看作被修改
						ValueNode vn = ValueNode.createConstantNode(v, dataTypeNode);
						branchGraph.addValueNode(vn);
						ValueEdge branchValueEdge = new ValueEdge(baseNode, vn, propertyEdge);
						branchGraph.addValueEdge(branchValueEdge);
						// need mergeIndex
						branchValueEdge.mergeIndex(valueEdge);
						branchGraph.reindexing(branchValueEdge);
					}
				}
			}

		});
	}

	// ValueEdge and ValueNode
	public void addValueEdgesBase(EObject base, TypedNode baseNode, TypedGraph baseGraph,
			Table<PropertyEdge, ValueNode, ValueEdge> valueEdgeTable, Map<PropertyEdge, ValueEdge> valueEdgeMap) {
		EClass cls = base.eClass();
		TypeNode typeNode = typeGraph.getTypeNode(cls.getName());

		cls.getEAllAttributes().forEach(a -> {
			DataTypeNode dataTypeNode = typeGraph.getDataTypeNode(a.getEAttributeType().getName());
			PropertyEdge propertyEdge = typeGraph.getPropertyEdge(typeNode, a.getName());

			if (a.isMany()) { // multi-valued
				@SuppressWarnings("unchecked")
				Collection<Object> values = (Collection<Object>) base.eGet(a);
				values.forEach(v -> {
					if (a.getEAttributeType() instanceof EEnum) {
						if (v instanceof Enumerator)
							v = ((Enumerator) v).getLiteral();
						else
							v = v.toString();
					}

					ValueNode vn = ValueNode.createConstantNode(v, dataTypeNode);
					baseGraph.addValueNode(vn);
					ValueEdge valueEdge = new ValueEdge(baseNode, vn, propertyEdge);
					baseGraph.addValueEdge(valueEdge);
					// record valueEdge
					valueEdgeTable.put(propertyEdge, vn, valueEdge);

				});
			} else { // single-valued
				Object v = base.eGet(a);
				if (v != null) {
					if (a.getEAttributeType() instanceof EEnum) {
						if (v instanceof Enumerator)
							v = ((Enumerator) v).getLiteral();
						else
							v = v.toString();
					}
					ValueNode vn = ValueNode.createConstantNode(v, dataTypeNode);
					baseGraph.addValueNode(vn);
					ValueEdge valueEdge = new ValueEdge(baseNode, vn, propertyEdge);
					baseGraph.addValueEdge(valueEdge);
					// record valueEdge
					valueEdgeMap.put(propertyEdge, valueEdge);

				}
			}

		});
	}

	public void saveSabModel(final URI uri, final TypedGraph graph) throws NothingReturnedException {
		EcoreModelUtil.save(uri, graph, null, getPackage("edu.ustb.lesley.college"));
	}

	public void registerCollegePackage(final URI metamodelUri) {
		registerPackage("edu.ustb.lesley.college", metamodelUri);
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
