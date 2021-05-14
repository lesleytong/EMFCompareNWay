package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Conflict;
import org.eclipse.emf.compare.ConflictKind;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.DifferenceSource;
import org.eclipse.emf.compare.DifferenceState;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.internal.spec.AttributeChangeSpec;
import org.eclipse.emf.compare.internal.spec.ReferenceChangeSpec;
import org.eclipse.emf.compare.merge.BatchMerger;
import org.eclipse.emf.compare.merge.IBatchMerger;
import org.eclipse.emf.compare.merge.IMerger;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import college.CollegePackage;
import edu.ustb.sei.mde.bxcore.exceptions.NothingReturnedException;
import edu.ustb.sei.mde.bxcore.util.EcoreModelUtil;
import edu.ustb.sei.mde.graph.type.PropertyEdge;
import edu.ustb.sei.mde.graph.type.TypeEdge;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import edu.ustb.sei.mde.graph.typedGraph.TypedGraph;
import my.MatchN;
import nway.ChangeTool;
import nway.NWay;

public class TestCollege {

	// 辅助cache()
	static int cacheStartIndex;

	static String NsURIName = "https://edu/ustb/lesley/college";
	static EPackage ep = CollegePackage.eINSTANCE;

	static ResourceSet resourceSet;
	static Resource baseResource;
	static Resource backupResource;

	static IBatchMerger merger = new BatchMerger(IMerger.RegistryImpl.createStandaloneInstance());

	static URI baseURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college.xmi");
	static URI backupURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college_backup.xmi");
	static URI m0URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college_m0.xmi");

	static URI branch1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college1.xmi");
	static URI branch2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college2.xmi");
	static URI branch3URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college3.xmi");
	static ArrayList<URI> uriList = new ArrayList<>();

	static URI metaModelURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\model\\college.ecore");
	static URI m1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college_m1.xmi");
	static URI m2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college_m2.xmi");

	public static void main(String[] args) {

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(NsURIName, ep);

		baseResource = resourceSet.getResource(baseURI, true);

		uriList.add(baseURI);
		uriList.add(branch1URI);
		uriList.add(branch2URI);
		uriList.add(branch3URI);

//		getM0();
//		getBranches();
//		testMerge();
//		testEquality(m0URI, m1URI);

//		testEMFCompare();
//		testEquality(m0URI, m2URI);

	}

	public static void getM0() {

		// 调用自动修改方法
		ChangeTool.changeForXMI(baseResource, m0URI);
		System.out.println("done");
	}

	public static void getBranches() {

		backupResource = resourceSet.getResource(backupURI, true);

		Random random = new Random();

		int N = uriList.size();

		// 利用EMF Compare比较得到m0与base之间的diffs
		Resource m0Resource = resourceSet.getResource(m0URI, true);
		IComparisonScope scope = new DefaultComparisonScope(baseResource, m0Resource, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		EList<Diff> diffs = comparison.getDifferences();

		// tmp
		diffs.forEach(d -> {
			System.out.println(d);
		});

		List<EObject> moveEObjects = new ArrayList<>();
		Map<EObject, EObject> eObjectMap = new HashMap<>();
		Map<EObject, Diff> eObjectDiffMap = new HashMap<>();
		Map<Diff, EObject> diffEObjectMap = new HashMap<>();
		MultiKeyMap<EObject, Boolean> checkNotMap = new MultiKeyMap<>(); // 其实可以没有true作为value，但如果用HashMap的话key只能唯一
		MultiKeyMap<EObject, Boolean> checkExistMap = new MultiKeyMap<>();

		diffs.forEach(d -> {
			if (d.getKind() == DifferenceKind.MOVE) {
				if (d instanceof AttributeChangeSpec) {
					// PENDING: 还未处理AttributeChangeSpec的MOVE
				} else if (d instanceof ReferenceChangeSpec) {
					ReferenceChangeSpec dr = (ReferenceChangeSpec) d;
					EReference reference = dr.getReference();
					if (reference.isContainment()) {
						Match match = d.getMatch().getComparison().getMatch(dr.getValue());
						EObject left = match.getLeft(); // baseResource中的元素
						EObject right = match.getRight();
						moveEObjects.add(left);
						eObjectMap.put(left, right);
						eObjectDiffMap.put(left, d);
						diffEObjectMap.put(d, left);
					}
				}
			}
		});

		if (moveEObjects.size() != 0) {
			// baseResource
			cacheStartIndex = 0;
			Map<EObject, Integer> baseIndex = new HashMap<>();
			baseResource.getContents().forEach(e -> {
				cache(e, baseIndex);
			});

			// m0Resource
			cacheStartIndex = 0;
			Map<EObject, Integer> m0Index = new HashMap<>();
			m0Resource.getContents().forEach(e -> {
				cache(e, m0Index);
			});

			for (int i = 0; i < moveEObjects.size() - 1; i++) {
				EObject first = moveEObjects.get(i);
				Integer firstIndexInBase = baseIndex.get(first); // base中的index
				Integer firstIndexInM0 = m0Index.get(eObjectMap.get(first)); // m0中的index
				for (int j = i + 1; j < moveEObjects.size(); j++) {
					EObject second = moveEObjects.get(j);
					Integer secondIndexInBase = baseIndex.get(second); // base中的index
					Integer secondIndexInM0 = m0Index.get(eObjectMap.get(second)); // m0中的index
					// base和m0中都是<x, y>，之后检查每个分支都不存在<y, x>
					// base中是<x, y>，m0中是<y, x>，之后检查任一分支中存在<y, x>
					int fsInBase = firstIndexInBase - secondIndexInBase;
					int fsInM0 = firstIndexInM0 - secondIndexInM0;
					if (fsInBase < 0 && fsInM0 < 0) {
						checkNotMap.put(first, second, true);
					} else if (fsInBase > 0 && fsInM0 > 0) {
						checkNotMap.put(second, first, true);
					} else if (fsInBase < 0 && fsInM0 > 0) {
						checkExistMap.put(second, first, false);
					} else if (fsInBase > 0 && fsInM0 < 0) {
						checkExistMap.put(first, second, false);
					}

				}
			}
		}

		// 分配diffs
		Collection<Collection<Diff>> collections = new HashSet<>();
		Collection<Diff> other = new HashSet<>();

		diffs.forEach(d -> {
			EList<Diff> requires = d.getRequires();
			EList<Diff> requiredBy = d.getRequiredBy();

			if (requires.size() != 0) {
				Collection<Diff> group = new HashSet<>();
				group.add(d);
				group.addAll(requires);
				collections.add(group);
			} else if (requiredBy.size() == 0) { // 必须用else if
				other.add(d);
			}
		});

		// diffList的初始化
		List<List<Diff>> diffList = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			List<Diff> diff = new ArrayList<>();
			diffList.add(diff);
		}

		// 将collections中的group随机分配给分支版本
		collections.forEach(group -> {
			double flag = random.nextDouble();
			if (flag >= 0.7) {
				diffList.get(1).addAll(group);
			} else if (flag <= 0.3) {
				diffList.get(2).addAll(group);
			} else {
				diffList.get(3).addAll(group);
			}
		});

		// 将other中的diff随机分配给分支版本
		other.forEach(d -> {
			double flag = random.nextDouble();
			if (flag >= 0.7) {
				diffList.get(1).add(d);
			} else if (flag <= 0.3) {
				diffList.get(2).add(d);
			} else {
				diffList.get(3).add(d);
			}
		});

		for (int i = 1; i < N; i++) {
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>diffBranch" + i);

			// 应用diff到相应的分支
			List<Diff> diffBranch = diffList.get(i);
			int originalSize = diffBranch.size();
			applyDiff(diffBranch, uriList.get(i));

			if (moveEObjects.size() != 0) {
				cacheStartIndex = 0;
				Map<EObject, Integer> branchIndex = new HashMap<>();
				baseResource.getContents().forEach(e -> {
					cache(e, branchIndex);
				});

				// 检查不能存在的
				checkNotMap.forEach((key, value) -> {
					Integer firstIndex = branchIndex.get(key.getKey(0));
					Integer secondIndex = branchIndex.get(key.getKey(1));
					if (firstIndex > secondIndex) {
						Diff boundDiff1 = eObjectDiffMap.get(key.getKey(0));
						Diff boundDiff2 = eObjectDiffMap.get(key.getKey(1));
						if (diffBranch.contains(boundDiff1) == false) {
							boundDiff1.setState(org.eclipse.emf.compare.DifferenceState.UNRESOLVED);
							diffBranch.add(boundDiff1);
						}
						if (diffBranch.contains(boundDiff2) == false) {
							boundDiff2.setState(org.eclipse.emf.compare.DifferenceState.UNRESOLVED);
							diffBranch.add(boundDiff2);
						}

					}
				});

				// 再次apply
				if (diffBranch.size() > originalSize) {
					applyDiff(diffBranch, uriList.get(i));
				}

				// 检查需要在一个分支中存在的
				checkExistMap.forEach((key, value) -> {
					// value为true后，之后就不用检查此键值对
					if (value == false) {
						EObject first = key.getKey(0);
						EObject second = key.getKey(1);
						Integer firstIndex = branchIndex.get(first);
						Integer secondIndex = branchIndex.get(second);
						if (firstIndex < secondIndex) {
							checkExistMap.put(key, true);
						}
					}
				});
			}

			// 恢复
			if (i != N - 1) {
				backup();
			}

		}

		if (moveEObjects.size() != 0) {
			// 若任何一个分支都不存在<y, x>，则在最后一个分支加入boundDiff
			List<Diff> diffBranch = diffList.get(N - 1);
			List<Diff> help = new ArrayList<>();
			int originalSize = diffBranch.size();
			checkExistMap.forEach((key, value) -> {
				if (value == false) {
					Diff boundDiff1 = eObjectDiffMap.get(key.getKey(0));
					Diff boundDiff2 = eObjectDiffMap.get(key.getKey(1));
					if (diffBranch.contains(boundDiff1) == false) {
						boundDiff1.setState(org.eclipse.emf.compare.DifferenceState.UNRESOLVED);
						diffBranch.add(boundDiff1);
						help.add(boundDiff1);
					}
					if (diffBranch.contains(boundDiff2) == false) {
						boundDiff2.setState(org.eclipse.emf.compare.DifferenceState.UNRESOLVED);
						diffBranch.add(boundDiff2);
						help.add(boundDiff2);
					}

				}
			});

			if (diffBranch.size() > originalSize) {
				// 最后一个分支再次apply
				applyDiff(diffBranch, uriList.get(N - 1));
			}

			// baseResource未恢复，因此为最后一个分支
			cacheStartIndex = 0;
			Map<EObject, Integer> branchIndex = new HashMap<>();
			baseResource.getContents().forEach(e -> {
				cache(e, branchIndex);
			});

			// 还要检查不能存在的
			checkNotMap.forEach((key, value) -> {
				Integer firstIndex = branchIndex.get(key.getKey(0));
				Integer secondIndex = branchIndex.get(key.getKey(1));
				if (firstIndex > secondIndex) {
					Diff boundDiff1 = eObjectDiffMap.get(key.getKey(0));
					Diff boundDiff2 = eObjectDiffMap.get(key.getKey(1));

					if (diffBranch.contains(boundDiff1) == false) {
						boundDiff1.setState(org.eclipse.emf.compare.DifferenceState.UNRESOLVED);
						diffBranch.add(boundDiff1);
					} else if (help.contains(boundDiff1) == false) {
						boundDiff1.setState(org.eclipse.emf.compare.DifferenceState.UNRESOLVED);
					}

					if (diffBranch.contains(boundDiff2) == false) {
						boundDiff2.setState(org.eclipse.emf.compare.DifferenceState.UNRESOLVED);
						diffBranch.add(boundDiff2);
					} else if (help.contains(boundDiff2) == false) {
						boundDiff2.setState(org.eclipse.emf.compare.DifferenceState.UNRESOLVED);
					}

				}
			});

			// 最后一个分支再次apply
			applyDiff(diffBranch, uriList.get(N - 1));
		}

		System.out.println("done");
	}

	public static void testMerge() {

		TypeGraph typeGraph = EcoreModelUtil.load(ep);

		long start = System.currentTimeMillis();
		NWay nWay = new NWay(NsURIName, ep, typeGraph);
		List<MatchN> matches = nWay.nMatch(uriList);
		// 指定了需要排序的边类型TypeEdge
		List<TypeEdge> typeEdgeList = typeGraph.getAllTypeEdges();
		// 指定了需要排序的边类型ValueEdge
		PropertyEdge propertyEdge = typeGraph.getPropertyEdge(typeGraph.getTypeNode("College"), "title");
		List<PropertyEdge> propertyEdgeList = new ArrayList<>();
		propertyEdgeList.add(propertyEdge);
		// 调用合并算法
		TypedGraph mergeModel = nWay.nMerge(matches, typeEdgeList, propertyEdgeList);
		long end = System.currentTimeMillis();
		System.out.println("总耗时：" + (end - start) + "ms.");
		System.out.println("****************************************************");

		try {
			nWay.saveModel(metaModelURI, m1URI, mergeModel);
		} catch (NothingReturnedException e) {
			e.printStackTrace();
		}

		System.out.println("done");

	}

	// 比较M1和M0
	public static void testEquality(URI leftURI, URI rightURI) {

		Resource leftResource = resourceSet.getResource(leftURI, true);
		Resource rightResource = resourceSet.getResource(rightURI, true);

		IComparisonScope scope = new DefaultComparisonScope(leftResource, rightResource, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);

		EList<Diff> diffs = comparison.getDifferences();
		System.out.println("diffs.size(): " + diffs.size());
		diffs.forEach(d -> {
			System.out.println(d);
		});

	}

	public static void testEMFCompare() {

		long start = System.currentTimeMillis();

		// 第一次leftResource为第一个分支
		Resource leftResource = resourceSet.getResource(branch1URI, true);
		Resource rightResource = null;

		for (int i = 2; i < uriList.size(); i++) {
			rightResource = resourceSet.getResource(uriList.get(i), true);
			leftResource = threeWay(leftResource, rightResource, baseResource);
		}

		long end = System.currentTimeMillis();
		System.out.println("迭代式三向合并总耗时：" + (end - start) + " ms.");

		ChangeTool.save(leftResource, m2URI);

		System.out.println("done");

	}

	public static Resource threeWay(Resource leftResource, Resource rightResource, Resource baseResource) {
		IComparisonScope scope = new DefaultComparisonScope(leftResource, rightResource, baseResource);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		Collection<Diff> rightDiffs = new HashSet<>();

		// tmp
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>matches: ");
		comparison.getMatches().forEach(m -> {
			System.out.println(m);
			m.getAllSubmatches().forEach(sm -> {
				System.out.println(sm);
			});
		});

		// tmp
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>conflict: ");
		comparison.getDifferences().forEach(d -> {
			Conflict conflict = d.getConflict();
			if (conflict != null && conflict.getKind() == ConflictKind.REAL) {
				System.out.println("有REAL冲突: " + conflict);
			}
		});

		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>rightDiffs: ");
		comparison.getDifferences().forEach(d -> {
			if (d.getSource() == DifferenceSource.RIGHT) {
				System.out.println("rightDiffs: " + d);
				rightDiffs.add(d);
			}
		});
		merger.copyAllRightToLeft(rightDiffs, null);

		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>intermediate: ");
		leftResource.getAllContents().forEachRemaining(e -> {
			System.out.println(e);
		});
		System.out.println("\n");

		return leftResource;
	}

	/** 将分配给分支的diff应用到base上，得到branch */
	public static void applyDiff(List<Diff> diff, URI branchURI) {
		diff.forEach(d -> {
			if (d.getState() == DifferenceState.UNRESOLVED) {
				System.out.println(d);
			}
		});
		System.out.println("------------------------------");
		merger.copyAllRightToLeft(diff, null);
		ChangeTool.save(baseResource, branchURI);
	}

	/** 恢复base */
	public static void backup() {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>backup");
		IComparisonScope backupScope = new DefaultComparisonScope(baseResource, backupResource, null);
		Comparison backupComparison = EMFCompare.builder().build().compare(backupScope);
		EList<Diff> backupDiffs = backupComparison.getDifferences();
		merger.copyAllRightToLeft(backupDiffs, null);
	}

	public static void cache(EObject e, Map<EObject, Integer> map) {
		map.put(e, cacheStartIndex++);
		if (e.eContents().size() != 0) {
			e.eContents().forEach(ec -> {
				cache(ec, map);
			});
		}
	}

}
