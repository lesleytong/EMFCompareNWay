package test;

/**
 * 测试EMF Compare
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.Monitor;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Conflict;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.internal.spec.MatchSpec;
import org.eclipse.emf.compare.internal.spec.ReferenceChangeSpec;
import org.eclipse.emf.compare.match.DefaultMatchEngine;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.NWayDefaultMatchEngine;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.merge.BatchMerger;
import org.eclipse.emf.compare.merge.IBatchMerger;
import org.eclipse.emf.compare.merge.IMerger;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.Test;

import college.CollegePackage;

@SuppressWarnings("restriction")
public class NWay {

	@Test
	public void nWay() {
		
		URI uriBase = URI.createFileURI("E:/eclipse-dsl-workspace/edu.ustb.lesley.college/src/test/change.xmi");
		URI uriBranch1 = URI.createFileURI("E:/eclipse-dsl-workspace/edu.ustb.lesley.college/src/test/change1.xmi");
		URI uriBranch2 = URI.createFileURI("E:/eclipse-dsl-workspace/edu.ustb.lesley.college/src/test/change2.xmi");
		URI uriBranch3 = URI.createFileURI("E:/eclipse-dsl-workspace/edu.ustb.lesley.college/src/test/change3.xmi");
		URI uriBranch4 = URI.createFileURI("E:/eclipse-dsl-workspace/edu.ustb.lesley.college/src/test/change4.xmi");

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
			
		// 此comparison是第一个分支与base比较的结果，作为全局的comparison，之后会向其中添加信息
		Comparison comparison = EMFCompare.builder().build()
				.compare(new DefaultComparisonScope(branchResource1, baseResource, null));
		
		// 为了处理ADD元素
		Map<Integer, EList<EObject>> addMap = new HashMap<>();
		addMap.put(1, new BasicEList<EObject>());
		for(Diff diff : comparison.getDifferences()) {
			if(diff.getKind() == DifferenceKind.ADD) {
				ReferenceChangeSpec diffADD = (ReferenceChangeSpec) diff;
				EObject left = diffADD.getMatch().getComparison().getMatch(diffADD.getValue()).getLeft();
				addMap.get(1).add(left);
			}
		}
				
		Comparison branchComparison = null;
		for (int i = 2; i < uriList.size(); i++) {				
			Resource branchResource = resourceSet.getResource(uriList.get(i), true);			
			branchComparison = EMFCompare.builder().build()
					.compare(new DefaultComparisonScope(branchResource, baseResource, null));

			// 为了处理ADD元素
			addMap.put(i, new BasicEList<EObject>());
			for(Diff diff : branchComparison.getDifferences()) {	// 这里是branchComparison
				if(diff.getKind() == DifferenceKind.ADD) {
					ReferenceChangeSpec diffADD = (ReferenceChangeSpec) diff;
					EObject left = diffADD.getMatch().getComparison().getMatch(diffADD.getValue()).getLeft();
					addMap.get(i).add(left);
				}
			}
			
			// 添加到全局differences和matches中
			comparison.getDifferences().addAll(branchComparison.getDifferences());
			comparison.getMatches().addAll(branchComparison.getMatches());
				
		}
		
		// preMap
		Map<EObject, EList<EObject>> preMap = new HashMap<>();
		classifyMatches(comparison.getMatches(), preMap);
						
		/** ADD元素的匹配 */				
		for(int i=1; i<uriList.size()-1; i++) {							
			Resource resourceI = resourceSet.getResource(uriList.get(i), true);									
			for(int j=i+1; j<uriList.size(); j++) {								
				Resource resourceJ = resourceSet.getResource(uriList.get(j), true);				
				IComparisonScope scope = new DefaultComparisonScope(resourceI, resourceJ, null);	
				EList<Match> preMatches = getPreMatches(preMap, new BasicEList<>());	
				
				NWayDefaultMatchEngine engine = (NWayDefaultMatchEngine) NWayDefaultMatchEngine.create(UseIdentifiers.NEVER);
				Comparison comparisonN = engine.matchN(scope, preMatches, new BasicMonitor());	
								
				System.out.println(i + " " + j + ": ");
				System.out.println("---------------------------------------------");
				printMatches(comparisonN.getMatches());
				System.out.println("---------------------------------------------");
				
			}
		}

	}
	
//	@Test
	public void threeWay() {

		URI uriBase = URI.createFileURI("E:/eclipse-dsl-workspace/edu.ustb.lesley.college/src/test/change.xmi");
		URI uriBranch1 = URI.createFileURI("E:/eclipse-dsl-workspace/edu.ustb.lesley.college/src/test/change1.xmi");
		URI uriBranch2 = URI.createFileURI("E:/eclipse-dsl-workspace/edu.ustb.lesley.college/src/test/change3.xmi");

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

//		// check that models are equal after batch merging
//		scope = new DefaultComparisonScope(branchResource1, branchResource2, null);		
//		Comparison assertionComparison = EMFCompare.builder().build().compare(scope);		
//		EList<Diff> assertionDifferences = assertionComparison.getDifferences();
//		System.out.println("after batch merging: " + assertionDifferences.size());
//		assertEquals(0, assertionDifferences.size());

	}
	
	
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
			EObject left = match.getLeft();	// 考虑有删除的情况
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

}
