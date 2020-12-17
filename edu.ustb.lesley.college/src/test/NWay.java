package test;

/**
 * 测试EMF Compare
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
		
		// 为了对matches分组存储
		Map<Integer, EList<Match>> matchesMap = new HashMap<>();

		// 此comparison是第一个分支与base比较的结果，作为全局的comparison，之后会向其中添加信息
		Comparison comparison = EMFCompare.builder().build()
				.compare(new DefaultComparisonScope(branchResource1, baseResource, null));
		
		// base与第一个分支的matches
		EList<Match> branch1Matches = new BasicEList<Match>();
		branch1Matches.addAll(comparison.getMatches());
		matchesMap.put(1, branch1Matches);

		// 为了处理ADD元素
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
			
			// base与其它分支的matches
			EList<Match> branchMatches = new BasicEList<Match>();
			branchMatches.addAll(branchComparison.getMatches());
			matchesMap.put(i, branchMatches);	
			
		
			// 为了处理ADD元素
			for (Diff diff : branchComparison.getDifferences()) { // 这里是branchComparison
				if (diff.getKind() == DifferenceKind.ADD) {
					ReferenceChangeSpec diffADD = (ReferenceChangeSpec) diff;
					EObject left = diffADD.getMatch().getComparison().getMatch(diffADD.getValue()).getLeft();
					allADDEList.add(left);
				}
			}


			// 添加到全局diffs和matches中
			comparison.getDifferences().addAll(branchComparison.getDifferences());
			// 就这里会把branchComparsion.getMatches都移动到compariosn.getMatches中，也会影响matchesMap
			comparison.getMatches().addAll(branchComparison.getMatches());	
					
		}

			
		/** 冲突检测 */
		// 拿到全局的diffs和matches
		EList<Diff> diffs = comparison.getDifferences();
		EList<Match> matches = comparison.getMatches();

		// diffMap
		Map<EObject, EList<Diff>> diffMap = new HashMap<>();
		classifyDiffs(diffs, diffMap);

		// 对每个diff再按照RIGHT source一致的进行addAll
		for (Diff diff : diffs) {
			EObject right = diff.getMatch().getRight();
			diff.getMatch().getDifferences().addAll(diffMap.get(right));
		}

		NWayMatchBasedConflictDetector detector = new NWayMatchBasedConflictDetector();
		detector.detect(comparison, new BasicMonitor());

		System.out.println("**********************************conflict");
		comparison.getConflicts().forEach(conflict -> System.out.println(conflict));
		System.out.println("******************************************");

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
				
				// 需要对其进行解析
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

				// 将新得到的关于ADD元素的匹配，放到allADDMatches中
				filerADDMatches(allADDMatches, comparisonN.getMatches(), preMatches);

			}
		}

		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++allADDMatches");
		printMatches(allADDMatches);
		System.out.println("-----------------------------------------------allADDMatches");
		
		/** 调用成团的方法 */
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
	
	/** 成团 */
	public static void makeClosure(EList<EObject> allADDEList, EList<Match> allADDMatches, Map<Integer, EList<EObject>> closureMap ) {

		List<Integer> record = new ArrayList<>();
		record.add(0);
		EObject eObject = allADDEList.get(0);
		EList<EObject> matchList = getSucMatchList(eObject, allADDMatches);

		if (Collections.disjoint(allADDEList, matchList) == true) { // 如果交集为空的话
			EList<EObject> closure = new BasicEList<>();
			closure.add(eObject);
			System.out.println("**********************************************");
			System.out.println(closure);
			System.out.println("**********************************************");
			

			EList<EObject> allADDEListCopy = new BasicEList<>();
			allADDEListCopy.addAll(allADDEList);
			allADDEListCopy.removeAll(closure);
			// 这里递归
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
						// 加入条件是必须与团中所有对象都能匹配上
						closure.add(matchEObject);
					} else if (existMatch(j, getMatchList(matchEObject, allADDMatches), matchList) == false) {
						// 如果之前在matchList中能有匹配的话肯定被加入团了，则不用record.add(j)
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
				// 这里递归
				if (allADDEListCopy.size() > 0) {
					makeClosure(allADDEListCopy, allADDMatches, closureMap);
				}

			}

		}

	}

	/** 如果与之前的对象存在匹配 */
	public static boolean existMatch(int j, EList<EObject> myMatchList, EList<EObject> preList) {
		for (int i = 0; i < j; i++) {
			if (myMatchList.contains(preList.get(i))) {
				return true;
			}
		}
		return false;
	}

	/** 检查前者为后者的子集 */
	public static boolean check(EList<EObject> closure, EList<EObject> matchList) {

		for (EObject c : closure) {
			if (matchList.contains(c) == false) {
				return false;
			}
		}

		return true;
	}

	/** 找allADDMatches中与此对象有关的匹配，向后看 */
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

	/** 找allADDMatches中与此对象有关的匹配 */
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

	/** 得到先前匹配 */
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
			EObject left = match.getLeft(); // 考虑有删除的情况. 会不会用diff.kind去判断会好些？
			if (right != null && left != null) {
				EList<Match> submatches = match.getSubmatches();
				if (submatches != null) {
					classifyMatches(submatches, preMap); // 递归
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
	
	// 最小编辑代价
    public static int minCost(String str1, String str2){
        if(str1 == null || str2 == null){
            return 0;
        }

        char[] chs1 = str1.toCharArray();
        char[] chs2 = str2.toCharArray();

        int row = str1.length()+1;
        int col = str2.length()+1;
        int[][] dp = new int[row][col];
        // dp矩阵的第一行
        for(int j=1; j<col; j++){
            dp[0][j] = j * 1;
        }
        // dp矩阵的第一列
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
