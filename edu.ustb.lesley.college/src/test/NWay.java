package test;

import static org.junit.Assert.assertEquals;

/**
 * 扩展EMF Compare
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import my.ComparisonN;
import my.MatchN;
import my.impl.ComparisonNImpl;
import my.impl.MatchNImpl;

import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Conflict;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.internal.spec.ComparisonSpec;
import org.eclipse.emf.compare.internal.spec.MatchSpec;
import org.eclipse.emf.compare.internal.spec.ReferenceChangeSpec;
import org.eclipse.emf.compare.match.eobject.EditionDistance;
import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher.DistanceFunction;
import org.eclipse.emf.compare.merge.BatchMerger;
import org.eclipse.emf.compare.merge.IBatchMerger;
import org.eclipse.emf.compare.merge.IMerger;
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
		URI uriBranch1 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add1.xmi");
		URI uriBranch2 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add2.xmi");
		URI uriBranch3 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add3.xmi");
		URI uriBranch4 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add4.xmi");

		ArrayList<URI> uriList = new ArrayList<>();
		uriList.add(uriBase);
		uriList.add(uriBranch1);
		uriList.add(uriBranch2);
		uriList.add(uriBranch3);
//		uriList.add(uriBranch4);

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put("https://edu/ustb/lesley/college", CollegePackage.eINSTANCE);

		Resource baseResource = resourceSet.getResource(uriList.get(0), true);
		Resource branchResource1 = resourceSet.getResource(uriList.get(1), true);

		// 方便传给我们的合并方法
		ArrayList<Resource> resources = new ArrayList<>();
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
				addDiffsMap.put(left, diff); // 保存一下此EObject对应的ADD diff
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
					addDiffsMap.put(left, diff); // 保存一下此EObject对应的ADD diff
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

		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++allADDMatches");
		printMatches(allADDMatches);
		System.out.println("-----------------------------------------------allADDMatches\n");

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

			// tmp: 打印所有的极大团
			System.out.println("+++++++++++++++++++++++++++clique");
			maximalCliques.forEach(clique -> {
				System.out.println(clique);
			});
			System.out.println("---------------------------clique\n");

			// 用EMF Compare自带的编辑距离计算每个极大团的分数
			Map<Integer, Info> map = new HashMap<>();
			for (int i = 0; i < maximalCliques.size(); i++) {
				List<EObject> List = maximalCliques.get(i);
				int sumCost = sumEditionDistance(List, table); // 这个团总的编辑代价
				Info info = new Info(List.size(), sumCost);
				map.put(i, info);
			}

			// tmp 打印一下map
			System.out.println("++++++++++++++++++++map");
			map.forEach((key, value) -> {
				System.out.print("key: " + key);
				System.out.print(" size: " + value.size);
				System.out.println(" sumMinCost: " + value.sumMinCost);
			});
			System.out.println("--------------------map\n");

			// 先比较size（值大的排前面），当size相同时再比较sumMinCost（值小的排前面）
			List<Integer> sortedList = map.entrySet().stream()
					.sorted(Entry.comparingByValue(
							Comparator.comparing(Info::getSize).reversed().thenComparing(Info::getMinCost)))
					.map(Map.Entry::getKey).collect(Collectors.toList());

			// tmp: 打印sortedList
			System.out.println("++++++++++++++++++++sortedList");
			sortedList.forEach(System.out::println);
			System.out.println("--------------------sortedList\n");

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

			// tmp: 打印更新后的sortedList
			System.out.println("++++++++++++++++++++更新后的sortedList");
			sortedList.forEach(System.out::println);
			System.out.println("--------------------更新后的sortedList\n");

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

		/** matchN传入我们的合并方法 */
		List<MatchN> matches = comparisonN.getMatches();
		Runner_New runner = new Runner_New();
//		Runner runner = new Runner();
		runner.testMerge(resources, matches);

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

//	 @Test
	public void threeWay() {

		URI uriBase = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add.xmi");
		URI uriBranch1 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add3.xmi");
		URI uriBranch2 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/add4.xmi");

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put("https://edu/ustb/lesley/college", CollegePackage.eINSTANCE);

		Resource baseResource = resourceSet.getResource(uriBase, true);
		Resource branchResource1 = resourceSet.getResource(uriBranch1, true);
		Resource branchResource2 = resourceSet.getResource(uriBranch2, true);

		IComparisonScope scope = new DefaultComparisonScope(branchResource1, branchResource2, baseResource);
		Comparison comparison = EMFCompare.builder().build().compare(scope);

		// check every match
		System.out.println("\n--------------------------------------match before merging");
		printMatches(comparison.getMatches());

		// check the state of every diff before merging
		System.out.println("\n--------------------------------------diff before merging");
		List<Diff> differences = comparison.getDifferences();

		for (Diff diff : differences) {
			Match m = diff.getMatch();
			System.out.println("//");

			// tmp
			if (diff instanceof ReferenceChangeSpec) {
				ReferenceChangeSpec r = (ReferenceChangeSpec) diff;
				EObject value = r.getValue();
				Match match = comparison.getMatch(value);
				System.out.println("//");
			}
		}

		// check the conflicts
		System.out.println("\n--------------------------------------conflicts");
		List<Conflict> conflicts = comparison.getConflicts();
		for (Conflict c : conflicts) {
			System.out.println(c.getKind());
			System.out.println(c.getDifferences());
		}

		if (conflicts.size() == 0) {
			// Let's merge every diff
			// in fact, three way is also use batch merging
			IBatchMerger merger = new BatchMerger(IMerger.RegistryImpl.createStandaloneInstance());
			merger.copyAllLeftToRight(differences, new BasicMonitor()); // 这里直接忽略右边的更改

			System.out.println("\n--------------------------------------diff after merging");
			for (Diff diff : differences) {
				System.out.println(diff);
			}

			// check Resource
			System.out.println("\n--------------------------------------branchResource1");
			TreeIterator<EObject> allContents = branchResource1.getAllContents();
			while (allContents.hasNext()) {
				EObject next = allContents.next();
				System.out.println(next.toString());
			}
			System.out.println("\n--------------------------------------branchResource2");
			allContents = branchResource2.getAllContents();
			while (allContents.hasNext()) {
				EObject next = allContents.next();
				System.out.println(next.toString());
			}
			// check that models are equal after batch merging
			scope = new DefaultComparisonScope(branchResource1, branchResource2, null);
			Comparison assertionComparison = EMFCompare.builder().build().compare(scope);
			List<Diff> assertionDifferences = assertionComparison.getDifferences();
			System.out.println("after batch merging: " + assertionDifferences.size());
			assertEquals(0, assertionDifferences.size());
		}
	}

}
