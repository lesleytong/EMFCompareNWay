package test;

/**
 * ����EMF Compare
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

		// Ϊ�˶�matches����洢
		Map<Integer, EList<Match>> matchesMap = new HashMap<>();

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
				EObject left = diffADD.getMatch().getComparison().getMatch(diffADD.getValue()).getLeft();
				addDiffsMap.put(left, diff); // ����һ�´�EObject��Ӧ��ADD diff
			}
		}

		Comparison branchComparison = null;
		for (int i = 2; i < uriList.size(); i++) {
			Resource branchResource = resourceSet.getResource(uriList.get(i), true);
			branchComparison = EMFCompare.builder().build()
					.compare(new DefaultComparisonScope(branchResource, baseResource, null));

			// base��������֧��matches
			EList<Match> branchMatches = new BasicEList<Match>(branchComparison.getMatches());
			matchesMap.put(i, branchMatches);

			// Ϊ�˴���ADDԪ�أ�base��������֧�ģ�
			for (Diff diff : branchComparison.getDifferences()) { // ������branchComparison
				if (diff.getKind() == DifferenceKind.ADD) {
					ReferenceChangeSpec diffADD = (ReferenceChangeSpec) diff;
					EObject left = diffADD.getMatch().getComparison().getMatch(diffADD.getValue()).getLeft();
					addDiffsMap.put(left, diff); // ����һ�´�EObject��Ӧ��ADD diff
				}
			}

			// ��ӵ�ȫ��diffs��matches��
			comparison.getDifferences().addAll(branchComparison.getDifferences());
			// ��������branchComparsion.getMatches���ƶ���compariosn.getMatches�У�Ҳ��Ӱ��matchesMap
			// ��˶���matchesMap���Ҳ�����������EList
			comparison.getMatches().addAll(branchComparison.getMatches());

		}

		/** ��ͻ��� */
		// �õ�ȫ�ֵ�diffs
		EList<Diff> diffs = comparison.getDifferences();

		// diffMap
		Map<EObject, EList<Diff>> diffMap = new HashMap<>();
		classifyDiffs(diffs, diffMap);

		// ��ÿ��diff�ٰ���RIGHT sourceһ�µģ�����addAll
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

			// Ϊ��֮�����༭����
			Table<Resource, Resource, EList<Match>> table = HashBasedTable.create();

			/** ADDԪ�ص�ƥ�� */
			EList<Match> allADDMatches = new BasicEList<>();

			for (int i = 1; i < uriList.size() - 1; i++) {
				Resource resourceI = resourceSet.getResource(uriList.get(i), true);

				// �����õ�base���֧i��ƥ����Ϣ
				EList<Match> matchListI = matchesMap.get(i);

				for (int j = i + 1; j < uriList.size(); j++) {
					Resource resourceJ = resourceSet.getResource(uriList.get(j), true);
					// �����õ�base���֧j��ƥ����Ϣ
					EList<Match> matchListJ = matchesMap.get(j);
					// ƥ����base��ͬһԪ�صķ��鵽һ��
					EList<Match> matchList = new BasicEList<Match>();
					matchList.addAll(matchListI);
					matchList.addAll(matchListJ);
					Map<EObject, EList<EObject>> preMap = new HashMap<>();
					groupMatches(matchList, preMap);
					// �õ�Ԥƥ�䣬������ADDԪ��֮���ƥ��
					EList<Match> preMatches = getPreMatches(preMap);

					// Ϊ��֮�����༭���ۣ�resourceI��resourceJ��Ϊ��
					table.put(resourceI, resourceJ, preMatches);
					table.put(resourceJ, resourceI, preMatches);	// ���ڼ������нڵ�����

					IComparisonScope scope = new DefaultComparisonScope(resourceI, resourceJ, null);
					NWayDefaultMatchEngine engine = (NWayDefaultMatchEngine) NWayDefaultMatchEngine
							.create(UseIdentifiers.NEVER);
					Comparison comparisonN = engine.matchN(scope, preMatches, new BasicMonitor());

					// ���µõ��Ĺ���ADDԪ�ص�ƥ�䣬�ŵ�allADDMatches��
					filerADDMatches(allADDMatches, comparisonN.getMatches(), preMatches);

				}
			}

			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++allADDMatches");
			printMatches(allADDMatches);
			System.out.println("-----------------------------------------------allADDMatches");

			/** ���� */
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
			
			// tmp: ��ӡ���еļ�����
			System.out.println("+++++++++++++++++++++++++++clique");
			maximalCliques.forEach(clique -> {
				System.out.println(clique);
			});
			System.out.println("---------------------------clique");

			// ��EMF Compare�ı༭�������
			Map<Integer, Info> map = new HashMap<>();
			for (int i = 0; i < maximalCliques.size(); i++) {
				EList<EObject> eList = maximalCliques.get(i);
				// ����������ܵı༭����
				int sumCost = sumEditionDistance(eList, table);
				Info info = new Info(eList.size(), sumCost);
				map.put(i, info);
			}

			// tmp ��ӡһ��map
			System.out.println("++++++++++++++++++++map");
			map.forEach((key, value) -> {
				System.out.print("key: " + key);
				System.out.print(" size: " + value.size);
				System.out.println(" sumMinCost: " + value.sumMinCost);
			});
			System.out.println("--------------------map");

			/** �Ƚ�size��ֵ�����ǰ�棩����size��ͬʱ�ٱȽ�sumMinCost��ֵС����ǰ�棩 */
			List<Integer> sortedList = map.entrySet().stream()
					.sorted(Entry.comparingByValue(
							Comparator.comparing(Info::getSize).reversed().thenComparing(Info::getMinCost)))
					.map(Map.Entry::getKey).collect(Collectors.toList());

			// tmp: ��ӡsortedList
			System.out.println("++++++++++++++++++++sortedList");
			sortedList.forEach(System.out::println);
			System.out.println("--------------------sortedList");

			/** ��ӵ����ͼ�� */
			for (int i = 0; i < sortedList.size() - 1; i++) {
				EList<EObject> preClique = maximalCliques.get(sortedList.get(i));
				for (int j = i + 1; j < sortedList.size(); j++) {
					EList<EObject> sucClique = maximalCliques.get(sortedList.get(j));
					if (Collections.disjoint(preClique, sucClique) == false) { // ���������Ϊ�յĻ�
						sortedList.remove(j);
						j--; // ����remove��������ǰ����һ��
					}
				}
			}

			/** ���ݸ��º��sortedMap��Ӧ��maximalCliques�� */
			// tmp: ��ӡ���º��sortedList
			System.out.println("++++++++++++++++++++���º��sortedList");
			sortedList.forEach(System.out::println);
			System.out.println("--------------------���º��sortedList");

			/** ȥ���ظ���ADD Diff */
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

			// ���±Ƚ�����diffs
			comparison.getDifferences().removeAll(removeDiffs);

			/** ִ�кϲ� Ӧ�������ǵĺϲ����� */

		}

	}

	/** �������ܵı༭���� */
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

	/** ����µõ���ADDԪ�ص�ƥ�� */
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

	/** ��diffs���� */
	public static void classifyDiffs(EList<Diff> diffs, Map<EObject, EList<Diff>> diffMap) {
		for (Diff diff : diffs) {
			EObject right = diff.getMatch().getRight(); // base�е�Ԫ��
			if (diffMap.get(right) == null) {
				EList<Diff> list = new BasicEList<>();
				list.add(diff);
				diffMap.put(right, list);
			} else {
				diffMap.get(right).add(diff);
			}
		}
	}

	/** �õ�Ԥƥ�� */
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
			EObject left = match.getLeft(); // ������ɾ�������. �᲻����diff.kindȥ�жϻ��Щ��
			if (right != null && left != null) {
				EList<Match> submatches = match.getSubmatches();
				if (submatches != null) {
					groupMatches(submatches, preMap); // �ݹ�
				}
				if (preMap.get(right) == null) {
					EList<EObject> eList = new BasicEList<>();
					eList.add(left);
					preMap.put(right, eList);
				} else {
					preMap.get(right).add(left); // right��base�е�Ԫ��
				}
			}
		}
	}

	/** ��ӡԪ��ƥ��Ľ�� */
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
//		merger.copyAllLeftToRight(differences, new BasicMonitor()); // ����ֱ�Ӻ����ұߵĸ���
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
