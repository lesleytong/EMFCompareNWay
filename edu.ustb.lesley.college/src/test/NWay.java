package test;

/**
 * ����EMF Compare
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.emf.compare.internal.spec.MatchSpec;
import org.eclipse.emf.compare.internal.spec.ReferenceChangeSpec;
import org.eclipse.emf.compare.match.NWayDefaultMatchEngine;
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
		EList<Match> branch1Matches = new BasicEList<Match>();
		branch1Matches.addAll(comparison.getMatches());
		matchesMap.put(1, branch1Matches);

		// Ϊ�˴���ADDԪ��
		EList<EObject> allADDEList = new BasicEList<>();
		for (Diff diff : comparison.getDifferences()) {
			if (diff.getKind() == DifferenceKind.ADD) {
				ReferenceChangeSpec diffADD = (ReferenceChangeSpec) diff;
				EObject left = diffADD.getMatch().getComparison().getMatch(diffADD.getValue()).getLeft();
				allADDEList.add(left);
			}
		}

	
		Comparison branchComparison = null;
		for (int i = 2; i < uriList.size(); i++) {
			Resource branchResource = resourceSet.getResource(uriList.get(i), true);
			branchComparison = EMFCompare.builder().build()
					.compare(new DefaultComparisonScope(branchResource, baseResource, null));
			
			// base��������֧��matches
			EList<Match> branchMatches = new BasicEList<Match>();
			branchMatches.addAll(branchComparison.getMatches());
			matchesMap.put(i, branchMatches);	
			
		
			// Ϊ�˴���ADDԪ��
			for (Diff diff : branchComparison.getDifferences()) { // ������branchComparison
				if (diff.getKind() == DifferenceKind.ADD) {
					ReferenceChangeSpec diffADD = (ReferenceChangeSpec) diff;
					EObject left = diffADD.getMatch().getComparison().getMatch(diffADD.getValue()).getLeft();
					allADDEList.add(left);
				}
			}


			// ��ӵ�ȫ��diffs��matches��
			comparison.getDifferences().addAll(branchComparison.getDifferences());
			// ��������branchComparsion.getMatches���ƶ���compariosn.getMatches�У�Ҳ��Ӱ��matchesMap
			comparison.getMatches().addAll(branchComparison.getMatches());	
					
		}

			
		/** ��ͻ��� */
		// �õ�ȫ�ֵ�diffs��matches
		EList<Diff> diffs = comparison.getDifferences();
		EList<Match> matches = comparison.getMatches();

		// diffMap
		Map<EObject, EList<Diff>> diffMap = new HashMap<>();
		classifyDiffs(diffs, diffMap);

		// ��ÿ��diff�ٰ���RIGHT sourceһ�µĽ���addAll
		for (Diff diff : diffs) {
			EObject right = diff.getMatch().getRight();
			diff.getMatch().getDifferences().addAll(diffMap.get(right));
		}

		NWayMatchBasedConflictDetector detector = new NWayMatchBasedConflictDetector();
		detector.detect(comparison, new BasicMonitor());

		System.out.println("**********************************conflict");
		comparison.getConflicts().forEach(conflict -> System.out.println(conflict));
		System.out.println("******************************************");

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
				
				// ��Ҫ������н���
				Map<EObject, EList<EObject>> preMap = new HashMap<>();
				EList<Match> matchList = new BasicEList<Match>();
				matchList.addAll(matchListI);
				matchList.addAll(matchListJ);
				classifyMatches(matchList, preMap);
				
				
				IComparisonScope scope = new DefaultComparisonScope(resourceI, resourceJ, null);
				EList<Match> preMatches = getPreMatches(preMap, new BasicEList<>());
				
				NWayDefaultMatchEngine engine = (NWayDefaultMatchEngine) NWayDefaultMatchEngine
						.create(UseIdentifiers.NEVER);
				
				// tmp
				if(i==1 && j==4) {
					System.out.println(i);
				}
				
				Comparison comparisonN = engine.matchN(scope, preMatches, new BasicMonitor());


				
//				System.out.println("---------------------------------------------");
//				System.out.println(i + " " + j + ": ");
//				printMatches(comparisonN.getMatches());
//				System.out.println("---------------------------------------------");

				// ���µõ��Ĺ���ADDԪ�ص�ƥ�䣬�ŵ�allADDMatches��
				filerADDMatches(allADDMatches, comparisonN.getMatches(), preMatches);

			}
		}

		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++allADDMatches");
		printMatches(allADDMatches);
		System.out.println("-----------------------------------------------allADDMatches");
		
		/** ���ó��ŵķ��� */
//		Map<Integer, EList<EObject>> closureMap = new HashMap<>();
//		makeClosure(allADDEList, allADDMatches, closureMap );
		
		EList<Match> edges = new BasicEList<>();	
		for (Match match : allADDMatches) {
			EObject left = match.getLeft();
			EObject right = match.getRight();
			if (left != null && right != null) {
				edges.add(match);
			}
		}
		
		MaximalCliquesWithoutPivot ff = new MaximalCliquesWithoutPivot();
		ff.initGraph(allADDEList, edges);
		EList<EList<EObject>> maximalCliques = new BasicEList<>();
		ff.Bron_KerboschPivotExecute(maximalCliques);

		System.out.println("+++++++++++++++++++++++++++clique");
		maximalCliques.forEach(clique -> {
			System.out.println(clique);
		});
		System.out.println("---------------------------clique");
		
		
		
		
		
		
	
	}
	
	/** ���� */
	public static void makeClosure(EList<EObject> allADDEList, EList<Match> allADDMatches, Map<Integer, EList<EObject>> closureMap ) {

		List<Integer> record = new ArrayList<>();
		record.add(0);
		EObject eObject = allADDEList.get(0);
		EList<EObject> matchList = getSucMatchList(eObject, allADDMatches);

		if (Collections.disjoint(allADDEList, matchList) == true) { // �������Ϊ�յĻ�
			EList<EObject> closure = new BasicEList<>();
			closure.add(eObject);
			System.out.println("**********************************************");
			System.out.println(closure);
			System.out.println("**********************************************");
			

			EList<EObject> allADDEListCopy = new BasicEList<>();
			allADDEListCopy.addAll(allADDEList);
			allADDEListCopy.removeAll(closure);
			// ����ݹ�
			if (allADDEListCopy.size() > 0) {
				makeClosure(allADDEListCopy, allADDMatches, closureMap);
			}

			return;
		}

		for (int i = 0; i < matchList.size(); i++) {

			if (record.contains(i)) {
				
				EList<EObject> closure = new BasicEList<>();

				for (int j = i; j < matchList.size(); j++) {

					EObject matchEObject = matchList.get(j);

					if (allADDEList.contains(matchEObject) == false) {
						continue;
					}

					if (check(closure, getMatchList(matchEObject, allADDMatches)) == true) {
						// ���������Ǳ������������ж�����ƥ����
						closure.add(matchEObject);
					} else if (existMatch(j, getMatchList(matchEObject, allADDMatches), matchList) == false) {
						// ���֮ǰ��matchList������ƥ��Ļ��϶����������ˣ�����record.add(j)
						record.add(j);
					}
				}

				closure.add(eObject);

				System.out.println("**********************************************");
				System.out.println(closure);
				System.out.println("**********************************************");
				
				// try
				EList<EObject> closureCopy = new BasicEList<EObject>();
				closureCopy.addAll(closureCopy);
				if(closureMap.get(i) == null) {
					closureMap.put(i, closureCopy);
				}else {					
					closureMap.get(i).addAll(closureCopy);
				}
			

				EList<EObject> allADDEListCopy = new BasicEList<>();
				allADDEListCopy.addAll(allADDEList);
				allADDEListCopy.removeAll(closure);
				// ����ݹ�
				if (allADDEListCopy.size() > 0) {
					makeClosure(allADDEListCopy, allADDMatches, closureMap);
				}

			}

		}

	}

	/** �����֮ǰ�Ķ������ƥ�� */
	public static boolean existMatch(int j, EList<EObject> myMatchList, EList<EObject> preList) {
		for (int i = 0; i < j; i++) {
			if (myMatchList.contains(preList.get(i))) {
				return true;
			}
		}
		return false;
	}

	/** ���ǰ��Ϊ���ߵ��Ӽ� */
	public static boolean check(EList<EObject> closure, EList<EObject> matchList) {

		for (EObject c : closure) {
			if (matchList.contains(c) == false) {
				return false;
			}
		}

		return true;
	}

	/** ��allADDMatches����˶����йص�ƥ�䣬��� */
	public static EList<EObject> getSucMatchList(EObject element, EList<Match> allADDMatches) {
		EList<EObject> myMatchEObjects = new BasicEList<>();
		for (Match match : allADDMatches) {
			EObject left = match.getLeft();
			EObject right = match.getRight();
			if (left == element && right != null) {
				myMatchEObjects.add(right);
			}
		}
		return myMatchEObjects;
	}

	/** ��allADDMatches����˶����йص�ƥ�� */
	public static EList<EObject> getMatchList(EObject element, EList<Match> allADDMatches) {
		EList<EObject> myMatchEObjects = new BasicEList<>();
		for (Match match : allADDMatches) {
			EObject left = match.getLeft();
			EObject right = match.getRight();
			if (left == element && right != null) {
				myMatchEObjects.add(right);
			} else if (right == element && left != null) {
				myMatchEObjects.add(left);
			}
		}
		return myMatchEObjects;
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

	/** �õ���ǰƥ�� */
	public static EList<Match> getPreMatches(Map<EObject, EList<EObject>> preMap, EList<Match> preMatches) {
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
	public static void classifyMatches(EList<Match> matches, Map<EObject, EList<EObject>> preMap) {
		for (Match match : matches) {
			EObject right = match.getRight();
			EObject left = match.getLeft(); // ������ɾ�������. �᲻����diff.kindȥ�жϻ��Щ��
			if (right != null && left != null) {
				EList<Match> submatches = match.getSubmatches();
				if (submatches != null) {
					classifyMatches(submatches, preMap); // �ݹ�
				}
				if (preMap.get(right) == null) {
					EList<EObject> eList = new BasicEList<>();
					eList.add(left);
					preMap.put(right, eList);
				} else {
					preMap.get(right).add(left);
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

//	@Test
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
	
	// ��С�༭����
    public static int minCost(String str1, String str2){
        if(str1 == null || str2 == null){
            return 0;
        }

        char[] chs1 = str1.toCharArray();
        char[] chs2 = str2.toCharArray();

        int row = str1.length()+1;
        int col = str2.length()+1;
        int[][] dp = new int[row][col];
        // dp����ĵ�һ��
        for(int j=1; j<col; j++){
            dp[0][j] = j * 1;
        }
        // dp����ĵ�һ��
        for(int i=1; i<row; i++){
            dp[i][0] = i * 1;
        }
        // dp[i][j]
        for(int i=1; i<row; i++){
            for(int j=1; j<col; j++){
                if(chs1[i-1] == chs2[j-1]){
                    dp[i][j] = dp[i-1][j-1];
                }else{
                    dp[i][j] = dp[i-1][j-1] + 1;
                }
                dp[i][j] = Math.min(dp[i][j], dp[i][j-1]+1);
                dp[i][j] = Math.min(dp[i][j], dp[i-1][j]+1);
            }
        }

        return dp[row-1][col-1];
    }

}
