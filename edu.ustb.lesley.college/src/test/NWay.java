package test;

import static org.junit.Assert.assertEquals;

/**
 * ��չEMF Compare
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

		// ���㴫�����ǵĺϲ�����
		ArrayList<Resource> resources = new ArrayList<>();
		resources.add(baseResource);
		resources.add(branchResource1);

		// Ϊ�˶�matches����洢
		Map<Integer, List<Match>> matchesMap = new HashMap<>();

		// ��comparison�ǵ�һ����֧��base�ȽϵĽ������Ϊȫ�ֵ�comparison��֮��������������Ϣ
		Comparison comparison = EMFCompare.builder().build()
				.compare(new DefaultComparisonScope(branchResource1, baseResource, null));

		// base���һ����֧��matches
		matchesMap.put(1, comparison.getMatches());

		// ��������ΪADD������diff
		Map<EObject, Diff> addDiffsMap = new HashMap<>();

		// Ϊ�˴���ADDԪ�أ�base���һ����֧�ģ�
		for (Diff diff : comparison.getDifferences()) {
			if (diff.getKind() == DifferenceKind.ADD) {
				ReferenceChangeSpec diffADD = (ReferenceChangeSpec) diff;
				EObject left = diffADD.getValue();
				addDiffsMap.put(left, diff); // ����һ�´�EObject��Ӧ��ADD diff
			}
		}

		Comparison branchComparison = null;
		for (int i = 2; i < uriList.size(); i++) {
			Resource branchResource = resourceSet.getResource(uriList.get(i), true);

			resources.add(branchResource);

			branchComparison = EMFCompare.builder().build()
					.compare(new DefaultComparisonScope(branchResource, baseResource, null));

			// base��������֧��matches
			List<Match> branchMatches = new ArrayList<Match>(branchComparison.getMatches());
			matchesMap.put(i, branchMatches);

			// Ϊ�˴���ADDԪ�أ�base��������֧�ģ�
			for (Diff diff : branchComparison.getDifferences()) { // ������branchComparison
				if (diff.getKind() == DifferenceKind.ADD) {
					ReferenceChangeSpec diffADD = (ReferenceChangeSpec) diff;
					EObject left = diffADD.getValue();
					addDiffsMap.put(left, diff); // ����һ�´�EObject��Ӧ��ADD diff
				}
			}

			// ��ӵ�ȫ��diffs��matches��
			// comparison.differences����һֱΪnull��Ȼ��comparison.getDifferences()���ǣ���
			comparison.getDifferences().addAll(branchComparison.getDifferences());

			// ��������branchComparsion.getMatches���ƶ���compariosn.getMatches�У�Ҳ��Ӱ��matchesMap
			// ��˶���matchesMap���Ҳ�����������List
			comparison.getMatches().addAll(branchComparison.getMatches());

		}

		// ��comparisonN����base�������֧��Ԫ��ƥ����Ϣ
		ComparisonN comparisonN = new ComparisonNImpl();
		Map<EObject, List<EObject>> haveBaseMap = new HashMap<>();
		groupMatches(comparison.getMatches(), haveBaseMap);
		haveBaseMap.forEach((key, value) -> {
			MatchN matchN = new MatchNImpl();
			matchN.setBase(key);
			matchN.getBranches().addAll(value);
			comparisonN.getMatches().add(matchN);
		});

		// Ϊ��֮�����༭����
		Table<Resource, Resource, List<Match>> table = HashBasedTable.create();

		/** ADDԪ�ص�ƥ�� */
		List<Match> allADDMatches = new ArrayList<>();

		for (int i = 1; i < uriList.size() - 1; i++) {
			Resource resourceI = resourceSet.getResource(uriList.get(i), true);

			// �����õ�base���֧i��ƥ����Ϣ
			List<Match> matchListI = matchesMap.get(i);

			for (int j = i + 1; j < uriList.size(); j++) {
				Resource resourceJ = resourceSet.getResource(uriList.get(j), true);
				// �����õ�base���֧j��ƥ����Ϣ
				List<Match> matchListJ = matchesMap.get(j);
				// ƥ����base��ͬһԪ�صķ��鵽һ��
				List<Match> matchList = new ArrayList<Match>();
				matchList.addAll(matchListI);
				matchList.addAll(matchListJ);
				Map<EObject, List<EObject>> preMap = new HashMap<>();
				groupMatches(matchList, preMap);
				// �õ�Ԥƥ�䣬������ADDԪ��֮���ƥ��
				List<Match> preMatches = getPreMatches(preMap);

				// Ϊ��֮�����༭���ۣ�resourceI��resourceJ��Ϊ��
				table.put(resourceI, resourceJ, preMatches);
				table.put(resourceJ, resourceI, preMatches); // ����֮����Լ������нڵ�����

				IComparisonScope scope = new DefaultComparisonScope(resourceI, resourceJ, null);
				NWayDefaultMatchEngine engine = (NWayDefaultMatchEngine) NWayDefaultMatchEngine
						.create(UseIdentifiers.NEVER);
				Comparison comparisonADD = engine.matchN(scope, preMatches, new BasicMonitor());

				// ���µõ��Ĺ���ADDԪ�ص�ƥ�䣬�ŵ�allADDMatches��
				filerADDMatches(allADDMatches, comparisonADD.getMatches(), preMatches);

			}
		}

		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++allADDMatches");
		printMatches(allADDMatches);
		System.out.println("-----------------------------------------------allADDMatches\n");

		/** ���� */
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

			// tmp: ��ӡ���еļ�����
			System.out.println("+++++++++++++++++++++++++++clique");
			maximalCliques.forEach(clique -> {
				System.out.println(clique);
			});
			System.out.println("---------------------------clique\n");

			// ��EMF Compare�Դ��ı༭�������ÿ�������ŵķ���
			Map<Integer, Info> map = new HashMap<>();
			for (int i = 0; i < maximalCliques.size(); i++) {
				List<EObject> List = maximalCliques.get(i);
				int sumCost = sumEditionDistance(List, table); // ������ܵı༭����
				Info info = new Info(List.size(), sumCost);
				map.put(i, info);
			}

			// tmp ��ӡһ��map
			System.out.println("++++++++++++++++++++map");
			map.forEach((key, value) -> {
				System.out.print("key: " + key);
				System.out.print(" size: " + value.size);
				System.out.println(" sumMinCost: " + value.sumMinCost);
			});
			System.out.println("--------------------map\n");

			// �ȱȽ�size��ֵ�����ǰ�棩����size��ͬʱ�ٱȽ�sumMinCost��ֵС����ǰ�棩
			List<Integer> sortedList = map.entrySet().stream()
					.sorted(Entry.comparingByValue(
							Comparator.comparing(Info::getSize).reversed().thenComparing(Info::getMinCost)))
					.map(Map.Entry::getKey).collect(Collectors.toList());

			// tmp: ��ӡsortedList
			System.out.println("++++++++++++++++++++sortedList");
			sortedList.forEach(System.out::println);
			System.out.println("--------------------sortedList\n");

			// ����sortedList
			for (int i = 0; i < sortedList.size() - 1; i++) {
				List<EObject> preClique = maximalCliques.get(sortedList.get(i));
				for (int j = i + 1; j < sortedList.size(); j++) {
					List<EObject> sucClique = maximalCliques.get(sortedList.get(j));
					if (Collections.disjoint(preClique, sucClique) == false) { // ���������Ϊ�յĻ�
						sortedList.remove(j);
						j--; // ����remove��������ǰ����һ��
					}
				}
			}

			// tmp: ��ӡ���º��sortedList
			System.out.println("++++++++++++++++++++���º��sortedList");
			sortedList.forEach(System.out::println);
			System.out.println("--------------------���º��sortedList\n");

			// ��comparisonN����һ��
			sortedList.forEach(i -> {
				// ���ݸ��º��sortedList��Ӧ��maximalCliques��
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

		/** matchN�������ǵĺϲ����� */
		List<MatchN> matches = comparisonN.getMatches();
		Runner_New runner = new Runner_New();
//		Runner runner = new Runner();
		runner.testMerge(resources, matches);

	}

	/** �������ܵı༭���� */
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

	/** ����µõ���ADDԪ�ص�ƥ�� */
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

	/** �õ�Ԥƥ�� */
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
			EObject left = match.getLeft(); // ������ɾ�������. �᲻����diff.kindȥ�жϻ��Щ��
			if (right != null && left != null) {
				List<Match> submatches = match.getSubmatches();
				if (submatches != null) {
					groupMatches(submatches, preMap); // �ݹ�
				}
				if (preMap.get(right) == null) {
					List<EObject> List = new ArrayList<>();
					List.add(left);
					preMap.put(right, List);
				} else {
					preMap.get(right).add(left); // right��base�е�Ԫ��
				}
			}
		}
	}

	/** ��ӡԪ��ƥ��Ľ�� */
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
			merger.copyAllLeftToRight(differences, new BasicMonitor()); // ����ֱ�Ӻ����ұߵĸ���

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
