package test;

/**
 * 测试EMF Compare
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Conflict;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.conflict.NWayMatchBasedConflictDetector;
import org.eclipse.emf.compare.internal.spec.ComparisonSpec;
import org.eclipse.emf.compare.internal.spec.MatchSpec;
import org.eclipse.emf.compare.internal.spec.ReferenceChangeSpec;
import org.eclipse.emf.compare.match.NWayDefaultMatchEngine;
import org.eclipse.emf.compare.match.eobject.EditionDistance;
import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher.DistanceFunction;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.Test;

import college.CollegePackage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

@SuppressWarnings("restriction")
public class NWay {

	@Test
	public void nWay() {

		URI uriBase = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add.xmi");
		URI uriBranch1 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add3.xmi");
		URI uriBranch2 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add2.xmi");
		URI uriBranch3 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add1.xmi");
		URI uriBranch4 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add4.xmi");

		ArrayList<URI> uriList = new ArrayList<>();

		uriList.add(uriBase);
		uriList.add(uriBranch1);
		uriList.add(uriBranch2);
		uriList.add(uriBranch3);
		uriList.add(uriBranch4);

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put("https://edu/ustb/lesley/college", CollegePackage.eINSTANCE);

		Resource baseResource = resourceSet.getResource(uriList.get(0), true);
		Resource branchResource1 = resourceSet.getResource(uriList.get(1), true);

		// 为了对matches分组存储
		Map<Integer, EList<Match>> matchesMap = new HashMap<>();

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
				EObject left = diffADD.getMatch().getComparison().getMatch(diffADD.getValue()).getLeft();
				addDiffsMap.put(left, diff); // 保存一下此EObject对应的ADD diff
			}
		}

		Comparison branchComparison = null;
		for (int i = 2; i < uriList.size(); i++) {
			Resource branchResource = resourceSet.getResource(uriList.get(i), true);
			branchComparison = EMFCompare.builder().build()
					.compare(new DefaultComparisonScope(branchResource, baseResource, null));

			// base与其它分支的matches
			EList<Match> branchMatches = new BasicEList<Match>(branchComparison.getMatches());
			matchesMap.put(i, branchMatches);

			// 为了处理ADD元素（base与其它分支的）
			for (Diff diff : branchComparison.getDifferences()) { // 这里是branchComparison
				if (diff.getKind() == DifferenceKind.ADD) {
					ReferenceChangeSpec diffADD = (ReferenceChangeSpec) diff;
					EObject left = diffADD.getMatch().getComparison().getMatch(diffADD.getValue()).getLeft();
					addDiffsMap.put(left, diff); // 保存一下此EObject对应的ADD diff
				}
			}

			// 添加到全局diffs和matches中
			comparison.getDifferences().addAll(branchComparison.getDifferences());
			// 就这里会把branchComparsion.getMatches都移动到compariosn.getMatches中，也会影响matchesMap
			// 因此对于matchesMap，我采用了新申请EList
			comparison.getMatches().addAll(branchComparison.getMatches());

		}

		/** 冲突检测 */
		// 拿到全局的diffs
		EList<Diff> diffs = comparison.getDifferences();

		// diffMap
		Map<EObject, EList<Diff>> diffMap = new HashMap<>();
		classifyDiffs(diffs, diffMap);

		// 对每个diff再按照RIGHT source一致的，进行addAll
		for (Diff diff : diffs) {
			EObject right = diff.getMatch().getRight();
			diff.getMatch().getDifferences().addAll(diffMap.get(right));
		}

		NWayMatchBasedConflictDetector detector = new NWayMatchBasedConflictDetector();
		detector.detect(comparison, new BasicMonitor());

		EList<Conflict> conflicts = comparison.getConflicts();

		System.out.println("+++++++++++++++++++++++++++++++++conflict");
		conflicts.forEach(c -> {
			System.out.println(c.getKind());
			System.out.println(c.getDifferences());
		});
		System.out.println("---------------------------------conflict");

		if (conflicts.size() == 0) {

			// 为了之后计算编辑代价
			Table<Resource, Resource, EList<Match>> table = HashBasedTable.create();

			/** ADD元素的匹配 */
			EList<Match> allADDMatches = new BasicEList<>();

			for (int i = 1; i < uriList.size() - 1; i++) {
				Resource resourceI = resourceSet.getResource(uriList.get(i), true);

				// 可以拿到base与分支i的匹配信息
				EList<Match> matchListI = matchesMap.get(i);

				for (int j = i + 1; j < uriList.size(); j++) {
					Resource resourceJ = resourceSet.getResource(uriList.get(j), true);
					// 可以拿到base与分支j的匹配信息
					EList<Match> matchListJ = matchesMap.get(j);
					// 匹配上base中同一元素的分组到一起
					EList<Match> matchList = new BasicEList<Match>();
					matchList.addAll(matchListI);
					matchList.addAll(matchListJ);
					Map<EObject, EList<EObject>> preMap = new HashMap<>();
					groupMatches(matchList, preMap);
					// 拿到预匹配，有助于ADD元素之后的匹配
					EList<Match> preMatches = getPreMatches(preMap);

					// 为了之后计算编辑代价，resourceI和resourceJ作为键
					table.put(resourceI, resourceJ, preMatches);
					table.put(resourceJ, resourceI, preMatches);	// 由于极大团中节点无序

					IComparisonScope scope = new DefaultComparisonScope(resourceI, resourceJ, null);
					NWayDefaultMatchEngine engine = (NWayDefaultMatchEngine) NWayDefaultMatchEngine
							.create(UseIdentifiers.NEVER);
					Comparison comparisonN = engine.matchN(scope, preMatches, new BasicMonitor());

					// 将新得到的关于ADD元素的匹配，放到allADDMatches中
					filerADDMatches(allADDMatches, comparisonN.getMatches(), preMatches);

				}
			}

			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++allADDMatches");
			printMatches(allADDMatches);
			System.out.println("-----------------------------------------------allADDMatches");

			/** 成团 */
			EList<Match> edges = new BasicEList<>();
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
			EList<EList<EObject>> maximalCliques = new BasicEList<>();
			ff.Bron_KerboschPivotExecute(maximalCliques);
			
			// tmp: 打印所有的极大团
			System.out.println("+++++++++++++++++++++++++++clique");
			maximalCliques.forEach(clique -> {
				System.out.println(clique);
			});
			System.out.println("---------------------------clique");

			// 用EMF Compare的编辑距离计算
			Map<Integer, Info> map = new HashMap<>();
			for (int i = 0; i < maximalCliques.size(); i++) {
				EList<EObject> eList = maximalCliques.get(i);
				// 计算这个团总的编辑代价
				int sumCost = sumEditionDistance(eList, table);
				Info info = new Info(eList.size(), sumCost);
				map.put(i, info);
			}

			// tmp 打印一下map
			System.out.println("++++++++++++++++++++map");
			map.forEach((key, value) -> {
				System.out.print("key: " + key);
				System.out.print(" size: " + value.size);
				System.out.println(" sumMinCost: " + value.sumMinCost);
			});
			System.out.println("--------------------map");

			/** 比较size（值大的排前面），当size相同时再比较sumMinCost（值小的排前面） */
			List<Integer> sortedList = map.entrySet().stream()
					.sorted(Entry.comparingByValue(
							Comparator.comparing(Info::getSize).reversed().thenComparing(Info::getMinCost)))
					.map(Map.Entry::getKey).collect(Collectors.toList());

			// tmp: 打印sortedList
			System.out.println("++++++++++++++++++++sortedList");
			sortedList.forEach(System.out::println);
			System.out.println("--------------------sortedList");

			/** 添加到结果图中 */
			for (int i = 0; i < sortedList.size() - 1; i++) {
				EList<EObject> preClique = maximalCliques.get(sortedList.get(i));
				for (int j = i + 1; j < sortedList.size(); j++) {
					EList<EObject> sucClique = maximalCliques.get(sortedList.get(j));
					if (Collections.disjoint(preClique, sucClique) == false) { // 如果交集不为空的话
						sortedList.remove(j);
						j--; // 由于remove后整体往前移了一个
					}
				}
			}

			/** 根据更新后的sortedMap对应到maximalCliques中 */
			// tmp: 打印更新后的sortedList
			System.out.println("++++++++++++++++++++更新后的sortedList");
			sortedList.forEach(System.out::println);
			System.out.println("--------------------更新后的sortedList");

			/** 去掉重复的ADD Diff */
			EList<EObject> removeEObjects = new BasicEList<>();
			for (int i = 0; i < sortedList.size(); i++) {
				EList<EObject> clique = maximalCliques.get(sortedList.get(i));
				for (int j = 1; j < clique.size(); j++) {
					removeEObjects.add(clique.get(j));
				}
			}

			EList<Diff> removeDiffs = new BasicEList<>();
			removeEObjects.forEach(e -> {
				removeDiffs.add(addDiffsMap.get(e));

			});

			// 更新比较器的diffs
			comparison.getDifferences().removeAll(removeDiffs);

			/** 执行合并 应该用我们的合并方法 */

		}

	}

	/** 计算团总的编辑代价 */
	public static int sumEditionDistance(EList<EObject> eList, Table<Resource, Resource, EList<Match>> table) {

		int sum = 0;
		DistanceFunction distanceFunction = new EditionDistance();

		for (int i = 0; i < eList.size() - 1; i++) {
			EObject eObjectI = eList.get(i);
			EObject eObjectJ = null;
			for (int j = i + 1; j < eList.size(); j++) {
				eObjectJ = eList.get(j);
				EList<Match> preMatches = table.get(eObjectI.eResource(), eObjectJ.eResource());
				Comparison comparisonTmp = new ComparisonSpec();
				comparisonTmp.getMatches().addAll(preMatches);
				sum += distanceFunction.distance(comparisonTmp, eObjectI, eObjectJ);
			}
		}

		return sum;
	}

	/** 获得新得到的ADD元素的匹配 */
	public static void filerADDMatches(EList<Match> allADDMatches, EList<Match> matches, EList<Match> preMatches) {
		matches.forEach(match -> {
			if (preMatches.contains(match) == false) {
				allADDMatches.add(match);
			}
			EList<Match> submatches = match.getSubmatches();
			if (submatches != null) {
				filerADDMatches(allADDMatches, submatches, preMatches);
			}
		});

	}

	/** 将diffs分类 */
	public static void classifyDiffs(EList<Diff> diffs, Map<EObject, EList<Diff>> diffMap) {
		for (Diff diff : diffs) {
			EObject right = diff.getMatch().getRight(); // base中的元素
			if (diffMap.get(right) == null) {
				EList<Diff> list = new BasicEList<>();
				list.add(diff);
				diffMap.put(right, list);
			} else {
				diffMap.get(right).add(diff);
			}
		}
	}

	/** 得到预匹配 */
	public static EList<Match> getPreMatches(Map<EObject, EList<EObject>> preMap) {
		EList<Match> preMatches = new BasicEList<>();
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
	public static void groupMatches(EList<Match> matches, Map<EObject, EList<EObject>> preMap) {
		for (Match match : matches) {
			EObject right = match.getRight();
			EObject left = match.getLeft(); // 考虑有删除的情况. 会不会用diff.kind去判断会好些？
			if (right != null && left != null) {
				EList<Match> submatches = match.getSubmatches();
				if (submatches != null) {
					groupMatches(submatches, preMap); // 递归
				}
				if (preMap.get(right) == null) {
					EList<EObject> eList = new BasicEList<>();
					eList.add(left);
					preMap.put(right, eList);
				} else {
					preMap.get(right).add(left); // right是base中的元素
				}
			}
		}
	}

	/** 打印元素匹配的结果 */
	public static void printMatches(EList<Match> matches) {
		for (Match match : matches) {
			System.out.println(match);
			EList<Match> submatches = match.getSubmatches();
			if (submatches != null) {
				printMatches(submatches);
			}
		}
	}

	// @Test
	public void threeWay() {

		URI uriBase = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add.xmi");
		URI uriBranch1 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add1.xmi");
		URI uriBranch2 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add4.xmi");

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put("https://edu/ustb/lesley/college", CollegePackage.eINSTANCE);

		Resource baseResource = resourceSet.getResource(uriBase, true);
		Resource branchResource1 = resourceSet.getResource(uriBranch1, true);
		Resource branchResource2 = resourceSet.getResource(uriBranch2, true);

		IComparisonScope scope = new DefaultComparisonScope(branchResource1, branchResource2, null); // tmp
		Comparison comparison = EMFCompare.builder().build().compare(scope);

		// check every match
		System.out.println("\n--------------------------------------match before merging");
		printMatches(comparison.getMatches());

		// check the state of every diff before merging
		System.out.println("\n--------------------------------------diff before merging");
		EList<Diff> differences = comparison.getDifferences();
		for (Diff diff : differences) {
			System.out.println(diff);
		}

		// check the conflicts
		System.out.println("\n--------------------------------------conflicts");
		EList<Conflict> conflicts = comparison.getConflicts();
		for (Conflict conflict : conflicts) {
			System.out.println(conflict);
		}

//		// Let's merge every diff
//		// in fact, three way is also use batch merging
//		IBatchMerger merger = new BatchMerger(IMerger.RegistryImpl.createStandaloneInstance());
//		merger.copyAllLeftToRight(differences, new BasicMonitor()); // 这里直接忽略右边的更改
//
//		System.out.println("\n--------------------------------------diff after merging");
//		for (Diff diff : differences) {
//			System.out.println(diff);
//		}
//
//		// check Resource
//		System.out.println("\n--------------------------------------branchResource1");
//		TreeIterator<EObject> allContents = branchResource1.getAllContents();
//		while (allContents.hasNext()) {
//			EObject next = allContents.next();
//			System.out.println(next.toString());
//		}
//		System.out.println("\n--------------------------------------branchResource2");
//		allContents = branchResource2.getAllContents();
//		while (allContents.hasNext()) {
//			EObject next = allContents.next();
//			System.out.println(next.toString());
//		}
//		// check that models are equal after batch merging
//		scope = new DefaultComparisonScope(branchResource1, branchResource2, null);		
//		Comparison assertionComparison = EMFCompare.builder().build().compare(scope);		
//		EList<Diff> assertionDifferences = assertionComparison.getDifferences();
//		System.out.println("after batch merging: " + assertionDifferences.size());
//		assertEquals(0, assertionDifferences.size());

	}

}
