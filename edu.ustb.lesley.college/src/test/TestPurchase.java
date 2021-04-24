package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceSource;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.merge.BatchMerger;
import org.eclipse.emf.compare.merge.IBatchMerger;
import org.eclipse.emf.compare.merge.IMerger;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import edu.ustb.sei.mde.bxcore.exceptions.NothingReturnedException;
import edu.ustb.sei.mde.graph.type.TypeEdge;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import edu.ustb.sei.mde.graph.typedGraph.TypedGraph;
import my.MatchN;
import nway.ChangeTool;
import nway.EcoreTypeGraph;
import nway.NWay;

public class TestPurchase {

	static ResourceSet resourceSet;
	static Resource baseResource;
	static Resource backupResource;
	
	static IBatchMerger merger = new BatchMerger(IMerger.RegistryImpl.createStandaloneInstance());

	static URI baseURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase.xmi");
	static URI backupURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase_backup.xmi");
	static URI m0URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase_m0.xmi");

	static URI branch1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase1.xmi");
	static URI branch2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase2.xmi");
	static URI branch3URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase3.xmi");
	static URI branch4URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase4.xmi");
	static URI branch5URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase5.xmi");
	static URI branch6URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase6.xmi");
	static ArrayList<URI> uriList = new ArrayList<>();

	static URI metaModelURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.compare\\model\\Ecore.ecore");
	static URI m1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase_m1.xmi");
	static URI m2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\purchase_m2.xmi");

	public static void main(String[] args) {

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		resourceSet = new ResourceSetImpl();
		baseResource = resourceSet.getResource(baseURI, true);
		backupResource = resourceSet.getResource(backupURI, true);

		merger = new BatchMerger(IMerger.RegistryImpl.createStandaloneInstance());

		uriList.add(baseURI);
		uriList.add(branch1URI);
		uriList.add(branch2URI);
		uriList.add(branch3URI);
		uriList.add(branch4URI);
		uriList.add(branch5URI);
		uriList.add(branch6URI);

//		getM0();
//		getBranches();
//		testMerge();
		testEquality(m0URI, m1URI);
		
//		testEMFCompare();
//		testEquality(m0URI, m2URI);

	}

	public static void getM0() {

		// 调用自动修改方法
		ChangeTool.changeForEcore(baseResource, m0URI);
		System.out.println("done");

	}

	public static void getBranches() {

		Random random = new Random();

		// 利用EMF Compare得到m0与base之间的diffs
		Resource m0Resource = resourceSet.getResource(m0URI, true);
		IComparisonScope scope = new DefaultComparisonScope(baseResource, m0Resource, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		EList<Diff> diffs = comparison.getDifferences();

		// 将需要绑定的diff分到一组，独立的存到other
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

		ArrayList<Diff> diff1 = new ArrayList();
		ArrayList<Diff> diff2 = new ArrayList();
		ArrayList<Diff> diff3 = new ArrayList();
		ArrayList<Diff> diff4 = new ArrayList();
		ArrayList<Diff> diff5 = new ArrayList();
		ArrayList<Diff> diff6 = new ArrayList();

		// 将collections中的group随机分配给分支版本
		collections.forEach(group -> {
			double flag = random.nextDouble();
			if (flag >= 0.9) {
				diff1.addAll(group);
			} else if (flag >= 0.75) {
				diff2.addAll(group);
			} else if (flag >= 0.55) {
				diff3.addAll(group);
			} else if (flag >= 0.35) {
				diff4.addAll(group);
			} else if (flag >= 0.15) {
				diff5.addAll(group);
			} else {
				diff6.addAll(group);
			}
		});

		// 将other中的diff随机分配给分支版本
		other.forEach(d -> {
			double flag = random.nextDouble();
			if (flag >= 0.9) {
				diff1.add(d);
			} else if (flag >= 0.75) {
				diff2.add(d);
			} else if (flag >= 0.55) {
				diff3.add(d);
			} else if (flag >= 0.35) {
				diff4.add(d);
			} else if (flag >= 0.15) {
				diff5.add(d);
			} else {
				diff6.add(d);
			}
		});

		// 在base上应用diff1，得到branch1
		applyDiff(diff1, branch1URI);
		// 恢复原来的base
		backup();

		// 在base上应用diff2，得到branch2
		applyDiff(diff2, branch2URI);
		// 恢复原来的base
		backup();

		// 在base上应用diff3，得到branch3
		applyDiff(diff3, branch3URI);
		// 恢复原来的base
		backup();

		// 在base上应用diff4，得到branch4
		applyDiff(diff4, branch4URI);
		// 恢复原来的base
		backup();

		// 在base上应用diff5，得到branch5
		applyDiff(diff5, branch5URI);
		// 恢复原来的base
		backup();

		// 在base上应用diff6，得到branch6
		applyDiff(diff6, branch6URI);

		System.out.println("done");
	}

	public static void testMerge() {

		EcoreTypeGraph et = new EcoreTypeGraph();
		TypeGraph typeGraph = et.getTypeGraph_Ecore();

		long start = System.currentTimeMillis();
		NWay nWay = new NWay(typeGraph);
		List<MatchN> matches = nWay.nMatch(uriList);
		// typeEdgeList指定需要进行排序的边类型
		TypeEdge typeEdge = typeGraph.getTypeEdge(typeGraph.getTypeNode("EClass"), "eAllEStructuralFeature");
		List<TypeEdge> typeEdgeList = new ArrayList<>();
		typeEdgeList.add(typeEdge);
		TypedGraph mergeModel = nWay.nMerge(matches, typeEdgeList, null);
		long end = System.currentTimeMillis();
		System.out.println("总耗时： " + (end - start) + " ms.");

		try {
			nWay.saveModel(metaModelURI, m1URI, mergeModel);
			System.out.println("done");
		} catch (NothingReturnedException e) {
			e.printStackTrace();
		}
	}

	/** 比较M1和M0 */
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
		
		// 第一次leftResource为branch1
		Resource leftResource = resourceSet.getResource(branch1URI, true);
		Resource rightResource = null;
		
		for(int i = 2; i<uriList.size(); i++) {
			rightResource = resourceSet.getResource(uriList.get(i), true);
			threeWay(leftResource, rightResource, baseResource);
			ChangeTool.save(leftResource, m2URI);
			leftResource = resourceSet.getResource(m2URI, true);
		}
				
		System.out.println("done");
		
	}

	public static void threeWay(Resource leftResource, Resource rightResource, Resource baseResource) {
		IComparisonScope scope = new DefaultComparisonScope(leftResource, rightResource, baseResource);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		Collection<Diff> rightDiffs = new HashSet<>();
		
		comparison.getDifferences().forEach(d -> {
			if(d.getSource()==DifferenceSource.RIGHT) {
				rightDiffs.add(d);
			}
		});
		merger.copyAllRightToLeft(rightDiffs, null);
		
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>leftResource: " );
		leftResource.getAllContents().forEachRemaining(e -> {
			System.out.println(e);
		});
	}

	/** 将分配给分支的diff应用到base上，得到branch */
	public static void applyDiff(ArrayList<Diff> diff, URI branchURI) {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>diff");
		diff.forEach(d -> {
			System.out.println(d);
		});
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

}
