package nway;

/**
 * 扩展EMF Compare
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.internal.spec.ComparisonSpec;
import org.eclipse.emf.compare.internal.spec.MatchSpec;
import org.eclipse.emf.compare.internal.spec.ReferenceChangeSpec;
import org.eclipse.emf.compare.match.eobject.EditionDistance;
import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher.DistanceFunction;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

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
import my.ComparisonN;
import my.MatchN;
import my.impl.ComparisonNImpl;
import my.impl.MatchNImpl;

@SuppressWarnings("restriction")
public class NWay extends XmuProgram {

	String NsURI = null;
	EPackage ep = null;
	ArrayList<URI> uriList = null;
	TypeGraph typeGraph = null;
	String metaModelPath = null;
	String mergeModelPath = null;
	ArrayList<Resource> resources = new ArrayList<>();

	public NWay(String NsURI, EPackage ep, ArrayList<URI> uriList, TypeGraph typeGraph, String metaModelPath,
			String mergeModelPath) {
		this.NsURI = NsURI;
		this.ep = ep;
		this.uriList = uriList;
		this.typeGraph = typeGraph;
		this.metaModelPath = metaModelPath;
		this.mergeModelPath = mergeModelPath;
	}

	public List<MatchN> nMatch() {

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(NsURI, ep);

		Resource baseResource = resourceSet.getResource(uriList.get(0), true);
		Resource branchResource1 = resourceSet.getResource(uriList.get(1), true);

		// 方便传给我们的合并方法
		resources.add(baseResource);
		resources.add(branchResource1);

		// 为了对matches分组存储
		Map<Integer, List<Match>> matchesMap = new HashMap<>();

		// 此comparison是第一个分支与base比较的结果，作为全局的comparison，之后会向其中添加信息
		Comparison comparison = EMFCompare.builder().build()
				.compare(new DefaultComparisonScope(branchResource1, baseResource, null));

		// base与第一个分支的matches
		matchesMap.put(1, comparison.getMatches());

		// 保存类型为ADD的所有diff
		Map<EObject, Diff> addDiffsMap = new HashMap<>();

		// 为了处理ADD元素（base与第一个分支的）
		for (Diff diff : comparison.getDifferences()) {
			if (diff.getKind() == DifferenceKind.ADD) {
				ReferenceChangeSpec diffADD = (ReferenceChangeSpec) diff;
				EObject left = diffADD.getValue();
				EObject right = diffADD.getMatch().getComparison().getMatch(left).getRight();
				if (right == null) { // 只有涉及到新加EObject的才能加入到addDiffsMap
					addDiffsMap.put(left, diff); // 保存一下此EObject对应的ADD diff
				}
			}
		}

		Comparison branchComparison = null;
		for (int i = 2; i < uriList.size(); i++) {
			Resource branchResource = resourceSet.getResource(uriList.get(i), true);

			resources.add(branchResource);

			branchComparison = EMFCompare.builder().build()
					.compare(new DefaultComparisonScope(branchResource, baseResource, null));

			// base与其它分支的matches
			List<Match> branchMatches = new ArrayList<Match>(branchComparison.getMatches());
			matchesMap.put(i, branchMatches);

			// 为了处理ADD元素（base与其它分支的）
			for (Diff diff : branchComparison.getDifferences()) { // 这里是branchComparison
				if (diff.getKind() == DifferenceKind.ADD) {
					ReferenceChangeSpec diffADD = (ReferenceChangeSpec) diff;
					EObject left = diffADD.getValue();
					EObject right = diffADD.getMatch().getComparison().getMatch(left).getRight();
					if (right == null) { // 只有涉及到新加EObject的才能加入到addDiffsMap
						addDiffsMap.put(left, diff); // 保存一下此EObject对应的ADD diff
					}
				}
			}

			// 添加到全局diffs和matches中
			// comparison.differences属性一直为null，然而comparison.getDifferences()不是！！
			comparison.getDifferences().addAll(branchComparison.getDifferences());

			// 就这里会把branchComparsion.getMatches都移动到compariosn.getMatches中，也会影响matchesMap
			// 因此对于matchesMap，我采用了新申请List
			comparison.getMatches().addAll(branchComparison.getMatches());

		}

		// 用comparisonN保存base与各个分支的元素匹配信息
		ComparisonN comparisonN = new ComparisonNImpl();
		Map<EObject, List<EObject>> haveBaseMap = new HashMap<>();
		groupMatches(comparison.getMatches(), haveBaseMap);
		haveBaseMap.forEach((key, value) -> {
			MatchN matchN = new MatchNImpl();
			matchN.setBase(key);
			matchN.getBranches().addAll(value);
			comparisonN.getMatches().add(matchN);
		});

		// 为了之后计算编辑代价
		Table<Resource, Resource, List<Match>> table = HashBasedTable.create();

		/** ADD元素的匹配 */
		if (addDiffsMap.size() == 1) {
			MatchN matchN = new MatchNImpl();
			matchN.getBranches().add(addDiffsMap.entrySet().stream().findFirst().get().getKey());
			comparisonN.getMatches().add(matchN);
		} else {
			List<Match> allADDMatches = new ArrayList<>();
			for (int i = 1; i < uriList.size() - 1; i++) {
				Resource resourceI = resourceSet.getResource(uriList.get(i), true);

				// 可以拿到base与分支i的匹配信息
				List<Match> matchListI = matchesMap.get(i);

				for (int j = i + 1; j < uriList.size(); j++) {
					Resource resourceJ = resourceSet.getResource(uriList.get(j), true);
					// 可以拿到base与分支j的匹配信息
					List<Match> matchListJ = matchesMap.get(j);
					// 匹配上base中同一元素的分组到一起
					List<Match> matchList = new ArrayList<Match>();
					matchList.addAll(matchListI);
					matchList.addAll(matchListJ);
					Map<EObject, List<EObject>> preMap = new HashMap<>();
					groupMatches(matchList, preMap);
					// 拿到预匹配，有助于ADD元素之后的匹配
					List<Match> preMatches = getPreMatches(preMap);

					// 为了之后计算编辑代价，resourceI和resourceJ作为键
					table.put(resourceI, resourceJ, preMatches);
					table.put(resourceJ, resourceI, preMatches); // 方便之后针对极大团中节点无序

					IComparisonScope scope = new DefaultComparisonScope(resourceI, resourceJ, null);
					NWayDefaultMatchEngine engine = (NWayDefaultMatchEngine) NWayDefaultMatchEngine
							.create(UseIdentifiers.NEVER);
					Comparison comparisonADD = engine.matchN(scope, preMatches, new BasicMonitor());

					// 将新得到的关于ADD元素的匹配，放到allADDMatches中
					filerADDMatches(allADDMatches, comparisonADD.getMatches(), preMatches);

				}
			}

			/** 成团 */
			if (allADDMatches.size() > 1) {
				List<Match> edges = new ArrayList<>();
				for (Match match : allADDMatches) {
					EObject left = match.getLeft();
					EObject right = match.getRight();
					if (left != null && right != null) {
						edges.add(match);
					}
				}

				// BKWithPivot
				MaximalCliquesWithPivot ff = new MaximalCliquesWithPivot();
				ff.initGraph(addDiffsMap.keySet(), edges);
				List<List<EObject>> maximalCliques = new ArrayList<>();
				ff.Bron_KerboschPivotExecute(maximalCliques);

				// 用EMF Compare自带的编辑距离计算每个极大团的分数
				Map<Integer, Info> map = new HashMap<>();
				for (int i = 0; i < maximalCliques.size(); i++) {
					List<EObject> List = maximalCliques.get(i);
					int sumCost = sumEditionDistance(List, table); // 这个团总的编辑代价
					Info info = new Info(List.size(), sumCost);
					map.put(i, info);
				}

				// 先比较size（值大的排前面），当size相同时再比较sumMinCost（值小的排前面）
				List<Integer> sortedList = map.entrySet().stream()
						.sorted(Entry.comparingByValue(
								Comparator.comparing(Info::getSize).reversed().thenComparing(Info::getMinCost)))
						.map(Map.Entry::getKey).collect(Collectors.toList());

				// 更新sortedList
				for (int i = 0; i < sortedList.size() - 1; i++) {
					List<EObject> preClique = maximalCliques.get(sortedList.get(i));
					for (int j = i + 1; j < sortedList.size(); j++) {
						List<EObject> sucClique = maximalCliques.get(sortedList.get(j));
						if (Collections.disjoint(preClique, sucClique) == false) { // 如果交集不为空的话
							sortedList.remove(j);
							j--; // 由于remove后整体往前移了一个
						}
					}
				}

				// 用comparisonN保存一下
				sortedList.forEach(i -> {
					// 根据更新后的sortedList对应到maximalCliques中
					List<EObject> List = maximalCliques.get(i);
					MatchN matchN = new MatchNImpl();
					matchN.getBranches().addAll(List);
					comparisonN.getMatches().add(matchN);
				});
			} else if (allADDMatches.size() == 1) {
				Match match = allADDMatches.get(0);
				EObject left = match.getLeft();
				EObject right = match.getRight();

				MatchN matchN = new MatchNImpl();
				if (left != null) {
					matchN.getBranches().add(left);
				}
				if (right != null) {
					matchN.getBranches().add(right);
				}
				comparisonN.getMatches().add(matchN);
			}
		}

		return comparisonN.getMatches();
	}

	/** MatchN传入我们的合并方法，进行diff和merge */
	public TypedGraph nMerge(List<MatchN> matches) {

		registerPackage(URI.createFileURI(metaModelPath));

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
				MultiKeyMap multiKeyMap = new MultiKeyMap();
				Map<PropertyEdge, ValueEdge> valueEdgeMap = new HashMap<>();
				addValueEdgesBase(base, baseNode, baseGraph, multiKeyMap, valueEdgeMap);

				for (EObject b : branches) {
					// TypedNode
					TypedGraph branchGraph = typedGraphMap.get(b.eResource());
					branchGraph.addTypedNode(baseNode);
					typedNodeMap.put(b, baseNode);

					// ValueEdge and ValueNode
					addValueEdges(b, baseNode, branchGraph, multiKeyMap, valueEdgeMap);

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
				MultiKeyMap multiKeyMap = new MultiKeyMap();
				Map<PropertyEdge, ValueEdge> valueEdgeMap = new HashMap<>();
				addValueEdgesBase(branchFirst, branchFirstNode, branchFirstGraph, multiKeyMap, valueEdgeMap);

				branches.parallelStream().skip(1).forEach(b -> {
					// TypedNode
					TypedGraph branchGraph = typedGraphMap.get(b.eResource());
					branchGraph.addTypedNode(branchFirstNode);
					typedNodeMap.put(b, branchFirstNode);

					// ValueEdge and ValueNode
					addValueEdges(b, branchFirstNode, branchGraph, multiKeyMap, valueEdgeMap);

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
				MultiKeyMap multiKeyMap = new MultiKeyMap();
				addTypedEdgesBase(base, baseNode, baseGraph, typedNodeMap, multiKeyMap);

				for (EObject b : branches) {
					TypedGraph branchGraph = typedGraphMap.get(b.eResource());
					// TypedEdge
					addTypedEdges(b, baseNode, branchGraph, typedNodeMap, multiKeyMap);
				}

			} else {

				EObject branchFirst = branches.get(0);
				TypedNode branchFirstNode = typedNodeMap.get(branchFirst);
				TypedGraph branchFirstGraph = typedGraphMap.get(branchFirst.eResource());

				// TypedEdge
				MultiKeyMap multiKeyMap = new MultiKeyMap();
				addTypedEdgesBase(branchFirst, branchFirstNode, branchFirstGraph, typedNodeMap, multiKeyMap);

				// TypedEdge
				branches.parallelStream().skip(1).forEach(b -> {
					TypedGraph branchGraph = typedGraphMap.get(b.eResource());
					addTypedEdges(b, branchFirstNode, branchGraph, typedNodeMap, multiKeyMap);
				});

			}
		}

		// tmp
		System.out.println("//");

		TypedGraph[] branchGraphs = new TypedGraph[resources.size() - 1];
		for (int i = 1; i < resources.size(); i++) {
			branchGraphs[i - 1] = typedGraphMap.get(resources.get(i));
		}

		try {
			TypedGraph baseGraph = typedGraphMap.get(resources.get(0));

			TypedGraph resultGraph = BXMerge3.merge(baseGraph, branchGraphs);

			HashMap<TypedEdge, TypedEdge> forceOrd = new HashMap<>();
			TypedGraph mergeModel = BXMerge3.threeOrder(baseGraph, resultGraph, forceOrd, branchGraphs);
			return mergeModel;

		} catch (NothingReturnedException e) {
			e.printStackTrace();
		}
		return null;

	}

	/** 计算团总的编辑代价 */
	public static int sumEditionDistance(List<EObject> List, Table<Resource, Resource, List<Match>> table) {

		int sum = 0;
		DistanceFunction distanceFunction = new EditionDistance();

		for (int i = 0; i < List.size() - 1; i++) {
			EObject eObjectI = List.get(i);
			EObject eObjectJ = null;
			for (int j = i + 1; j < List.size(); j++) {
				eObjectJ = List.get(j);
				List<Match> preMatches = table.get(eObjectI.eResource(), eObjectJ.eResource());
				Comparison comparisonTmp = new ComparisonSpec();
				comparisonTmp.getMatches().addAll(preMatches);
				sum += distanceFunction.distance(comparisonTmp, eObjectI, eObjectJ);
			}
		}

		return sum;
	}

	/** 获得新得到的ADD元素的匹配 */
	public static void filerADDMatches(List<Match> allADDMatches, List<Match> matches, List<Match> preMatches) {
		matches.forEach(match -> {
			if (preMatches.contains(match) == false) {
				allADDMatches.add(match);
			}
			List<Match> submatches = match.getSubmatches();
			if (submatches != null) {
				filerADDMatches(allADDMatches, submatches, preMatches);
			}
		});

	}

	/** 得到预匹配 */
	public static List<Match> getPreMatches(Map<EObject, List<EObject>> preMap) {
		List<Match> preMatches = new ArrayList<>();
		preMap.forEach((key, value) -> {
			for (int i = 0; i < value.size(); i++) {
				for (int j = i + 1; j < value.size(); j++) {
					Match match = new MatchSpec();
					match.setLeft(value.get(i));
					match.setRight(value.get(j));
					match.setOrigin(null);
					preMatches.add(match);
				}
			}
		});
		return preMatches;
	}

	/** preMap */
	public static void groupMatches(List<Match> matches, Map<EObject, List<EObject>> preMap) {
		for (Match match : matches) {
			EObject right = match.getRight();
			EObject left = match.getLeft(); // 考虑有删除的情况. 会不会用diff.kind去判断会好些？
			if (right != null && left != null) {
				List<Match> submatches = match.getSubmatches();
				if (submatches != null) {
					groupMatches(submatches, preMap); // 递归
				}
				if (preMap.get(right) == null) {
					List<EObject> List = new ArrayList<>();
					List.add(left);
					preMap.put(right, List);
				} else {
					preMap.get(right).add(left); // right是base中的元素
				}
			}
		}
	}

	/** 打印元素匹配的结果 */
	public static void printMatches(List<Match> matches) {
		for (Match match : matches) {
			System.out.println(match);
			List<Match> submatches = match.getSubmatches();
			if (submatches != null) {
				printMatches(submatches);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void addTypedEdges(EObject b, TypedNode baseNode, TypedGraph branchGraph,
			HashMap<EObject, TypedNode> typedNodeMap, MultiKeyMap multiKeyMap) {
		EClass cls = b.eClass();
		TypeNode typeNode = typeGraph.getTypeNode(cls.getName());

		cls.getEAllReferences().forEach(r -> {
			TypeEdge typeEdge = typeGraph.getTypeEdge(typeNode, r.getName());
			if (r.isMany()) { // multi-reference
				Collection<EObject> targets = (Collection<EObject>) b.eGet(r);

				targets.forEach(t -> {
					TypedNode targetNode = typedNodeMap.get(t);

					if (targetNode != null) {
						TypedEdge typedEdge = (TypedEdge) multiKeyMap.get(targetNode, typeEdge);

						if (typedEdge != null) {
							branchGraph.addTypedEdge(typedEdge);

						} else { // 不视作被修改

							TypedEdge typedEdgeBranch = new TypedEdge(baseNode, targetNode, typeEdge);
							branchGraph.addTypedEdge(typedEdgeBranch);
						}
					}

					if (r.isContainment()) {
						addTypedEdges(t, baseNode, branchGraph, typedNodeMap, multiKeyMap);
					}

				});
			} else { // single-reference
				EObject t = (EObject) b.eGet(r);
				if (t != null) {
					TypedNode targetNode = typedNodeMap.get(t);

					if (targetNode != null) {
						TypedEdge typedEdge = (TypedEdge) multiKeyMap.get(targetNode, typeEdge);

						if (typedEdge != null) {
							branchGraph.addTypedEdge(typedEdge);

						} else { // 也不视作被修改
							TypedEdge typedEdgeBranch = new TypedEdge(baseNode, targetNode, typeEdge);

							branchGraph.addTypedEdge(typedEdgeBranch);
						}
					}

					if (r.isContainment()) {
						addTypedEdges(t, baseNode, branchGraph, typedNodeMap, multiKeyMap);
					}
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void addTypedEdgesBase(EObject base, TypedNode baseNode, TypedGraph baseGraph,
			HashMap<EObject, TypedNode> typedNodeMap, MultiKeyMap multiKeyMap) {
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
						multiKeyMap.put(targetNode, typeEdge, typedEdge);

						if (r.isContainment()) {
							addTypedEdgesBase(t, baseNode, baseGraph, typedNodeMap, multiKeyMap);
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
						multiKeyMap.put(targetNode, typeEdge, typedEdge);

						if (r.isContainment()) {
							addTypedEdgesBase(t, baseNode, baseGraph, typedNodeMap, multiKeyMap);
						}
					}
				}
			}
		});
	}

	public void addValueEdges(EObject b, TypedNode baseNode, TypedGraph branchGraph,
			MultiKeyMap multiKeyMap, Map<PropertyEdge, ValueEdge> valueEdgeMap) {
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
					ValueEdge valueEdge = (ValueEdge) multiKeyMap.get(vn, propertyEdge);
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
			MultiKeyMap multiKeyMap, Map<PropertyEdge, ValueEdge> valueEdgeMap) {
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
					multiKeyMap.put(vn, propertyEdge, valueEdge);

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

	public void saveModel(final URI uri, final TypedGraph graph) throws NothingReturnedException {
		EcoreModelUtil.save(uri, graph, null, getPackage(NsURI));
	}

	public void registerPackage(final URI metamodelUri) {
		registerPackage(NsURI, metamodelUri);
	}

	public void print(TypedGraph typedGraph) {
		System.out.println("TypedNodes: " + typedGraph.getAllTypedNodes().toString());
		System.out.println("TypedEdges: " + typedGraph.getAllTypedEdges().toString());
		System.out.println("ValueNodes: " + typedGraph.getAllValueNodes().toString());
		System.out.println("ValueEdges: " + typedGraph.getAllValueEdges().toString());
		System.out.println("*********************************************************************");
		System.out.println();
	}

}
