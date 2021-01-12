package test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import com.google.common.collect.Table;

import edu.ustb.sei.mde.bxcore.XmuCoreUtils;
import edu.ustb.sei.mde.bxcore.util.EcoreModelUtil;
import edu.ustb.sei.mde.graph.type.DataTypeNode;
import edu.ustb.sei.mde.graph.type.PropertyEdge;
import edu.ustb.sei.mde.graph.type.TypeEdge;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import edu.ustb.sei.mde.graph.type.TypeNode;
import edu.ustb.sei.mde.graph.typedGraph.TypedEdge;
import edu.ustb.sei.mde.graph.typedGraph.TypedGraph;
import edu.ustb.sei.mde.graph.typedGraph.TypedNode;
import edu.ustb.sei.mde.graph.typedGraph.ValueEdge;
import edu.ustb.sei.mde.graph.typedGraph.ValueNode;

public class EcoreModelUtilN extends EcoreModelUtil {

	static public TypedGraph load(List<EObject> roots, TypeGraph typeGraph, Map<EObject, TypedNode> typedNodeMap,
			Table<TypedNode, PropertyEdge, ValueEdge> valueEdgeTable,
			Table<EObject, EReference, TypedEdge> typedEdgeTable) {

		TypedGraph graph = new TypedGraph(typeGraph);

		roots.forEach(root -> addTypedNode(root, typeGraph, graph, typedNodeMap, valueEdgeTable));

		roots.forEach(root -> root.eAllContents()
				.forEachRemaining(o -> addTypedNode(o, typeGraph, graph, typedNodeMap, valueEdgeTable)));

		roots.forEach(root -> addTypedEdges(root, typeGraph, graph, typedNodeMap, typedEdgeTable));

		return graph;
	}

	private static void addTypedNode(EObject node, TypeGraph typeGraph, TypedGraph graph,
			Map<EObject, TypedNode> typedNodeMap, Table<TypedNode, PropertyEdge, ValueEdge> valueEdgeTable) {

		TypeNode typeNode = typeGraph.getTypeNode(node.eClass().getName());
		TypedNode rootNode = new TypedNode(typeNode);
		graph.addTypedNode(rootNode);
		typedNodeMap.put(node, rootNode);

		EClass cls = node.eClass();
		cls.getEAllAttributes().forEach(a -> {
			DataTypeNode dataTypeNode = typeGraph.getDataTypeNode(a.getEAttributeType().getName());
			PropertyEdge propertyEdge = typeGraph.getPropertyEdge(typeNode, a.getName());

			if (a.isMany()) {
				@SuppressWarnings("unchecked")
				Collection<Object> values = (Collection<Object>) node.eGet(a);
				values.forEach(v -> {
					if (a.getEAttributeType() instanceof EEnum) {
						if (v instanceof Enumerator)
							v = ((Enumerator) v).getLiteral();
						else
							v = v.toString();
					}

					ValueNode vn = ValueNode.createConstantNode(v, dataTypeNode);
					graph.addValueNode(vn);
					ValueEdge valueEdge = new ValueEdge(rootNode, vn, propertyEdge);
					graph.addValueEdge(valueEdge);
					// record valueEdge
					valueEdgeTable.put(rootNode, propertyEdge, valueEdge);

				});
			} else {
				Object v = node.eGet(a);
				if (v != null) {
					if (a.getEAttributeType() instanceof EEnum) {
						if (v instanceof Enumerator)
							v = ((Enumerator) v).getLiteral();
						else
							v = v.toString();
					}
					ValueNode vn = ValueNode.createConstantNode(v, dataTypeNode);
					graph.addValueNode(vn);
					ValueEdge valueEdge = new ValueEdge(rootNode, vn, propertyEdge);
					graph.addValueEdge(valueEdge);
					// record valueEdge
					valueEdgeTable.put(rootNode, propertyEdge, valueEdge);

				}

			}

		});
	}

	@SuppressWarnings("unchecked")
	private static void addTypedEdges(EObject root, TypeGraph typeGraph, TypedGraph graph,
			Map<EObject, TypedNode> typedNodeMap, Table<EObject, EReference, TypedEdge> typedEdgeTable) {
		EClass cls = root.eClass();
		TypeNode typeNode = typeGraph.getTypeNode(cls.getName());

		cls.getEAllReferences().forEach(r -> {
			TypeEdge typeEdge = typeGraph.getTypeEdge(typeNode, r.getName());
			if (r.isMany()) {
				Collection<EObject> targets = (Collection<EObject>) root.eGet(r);
				targets.forEach(t -> {
					TypedNode targetNode = typedNodeMap.get(t);
					if (targetNode == null) {
						XmuCoreUtils.log(Level.WARNING,
								"The target node is not registered. The loader will ignore this edge: " + r, null);
					} else {
						TypedEdge typedEdge = new TypedEdge(typedNodeMap.get(root), targetNode, typeEdge);
						graph.addTypedEdge(typedEdge);
						// record typedEdge				
						typedEdgeTable.put(t, r, typedEdge);	// 考虑不完善：Lina和Lesley，Lina和Mars之间的reference是一致的
						
						if (r.isContainment()) {
							addTypedEdges(t, typeGraph, graph, typedNodeMap, typedEdgeTable);
						}
					}
				});
			} else {
				EObject t = (EObject) root.eGet(r);
				if (t != null) {
					TypedNode targetNode = typedNodeMap.get(t);
					if (targetNode == null) {
						XmuCoreUtils.log(Level.WARNING,
								"The target node is not registered. The loader will ignore this edge: " + r, null);
					} else {
						TypedEdge typedEdge = new TypedEdge(typedNodeMap.get(root), targetNode, typeEdge);
						graph.addTypedEdge(typedEdge);
						// record typedEdge
						typedEdgeTable.put(t, r, typedEdge);

						if (r.isContainment())
							addTypedEdges(t, typeGraph, graph, typedNodeMap, typedEdgeTable);
					}
				}
			}
		});
	}
}
