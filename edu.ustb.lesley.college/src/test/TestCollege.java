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
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.merge.BatchMerger;
import org.eclipse.emf.compare.merge.IBatchMerger;
import org.eclipse.emf.compare.merge.IMerger;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import college.CollegePackage;
import edu.ustb.sei.mde.bxcore.exceptions.NothingReturnedException;
import edu.ustb.sei.mde.bxcore.util.EcoreModelUtil;
import edu.ustb.sei.mde.graph.type.TypeEdge;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import edu.ustb.sei.mde.graph.typedGraph.TypedGraph;
import my.MatchN;
import nway.ChangeTool;
import nway.NWay;

public class TestCollege {

	static String NsURIName = "https://edu/ustb/lesley/college";
	static EPackage ep = CollegePackage.eINSTANCE;
	static ResourceSet resourceSet;

	static URI baseURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college.xmi");
	static URI backupURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college_backup.xmi");
	static URI m0URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college_m0.xmi");

	static URI branch1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college1.xmi");
	static URI branch2URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college2.xmi");
	static URI branch3URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college3.xmi");

	static URI metaModelURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\model\\college.ecore");
	static URI m1URI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\test\\college_m1.xmi");

	public static void main(String[] args) {

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(NsURIName, ep);

//		getM0();
//		getBranches();
//		testMerge();
		testEquality();

	}

	public static void getM0() {

		Resource baseResource = resourceSet.getResource(baseURI, true);
		// 调用自动修改方法
		ChangeTool.changeForXMI(baseResource);
		ChangeTool.save(baseResource, m0URI);
		System.out.println("done");
	}

	public static void getBranches() {

		Random random = new Random();
		IBatchMerger merger = new BatchMerger(IMerger.RegistryImpl.createStandaloneInstance());

		// 方便恢复baseResource
		IComparisonScope backupScope;
		Comparison backupComparison;
		EList<Diff> backupDiffs;
		Resource backupResource = resourceSet.getResource(backupURI, true);

		// 利用EMF Compare比较得到m0与base之间的diffs
		Resource baseResource = resourceSet.getResource(baseURI, true);
		Resource m0Resource = resourceSet.getResource(m0URI, true);

		IComparisonScope scope = new DefaultComparisonScope(m0Resource, baseResource, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		EList<Diff> diffs = comparison.getDifferences();

		Collection<Collection<Diff>> collections = new HashSet<>();
		Collection<Diff> other = new HashSet<>();

		diffs.forEach(d -> {
			System.out.println("d: " + d);
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
		collections.add(other);

		ArrayList<Diff> diff1 = new ArrayList();
		ArrayList<Diff> diff2 = new ArrayList();
		ArrayList<Diff> diff3 = new ArrayList();

		// 将collections中的group随机分配给三个分支版本
		collections.forEach(group -> {
			double flag = random.nextDouble();
			if (flag >= 0.7) {
				diff1.addAll(group);
			} else if (flag <= 0.3) {
				diff2.addAll(group);
			} else {
				diff3.addAll(group);
			}
		});

		// 将other中的diff随机分配给三个分支版本
		other.forEach(d -> {
			double flag = random.nextDouble();
			if (flag >= 0.7) {
				diff1.add(d);
			} else if (flag <= 0.3) {
				diff2.add(d);
			} else {
				diff3.add(d);
			}
		});

		// 在base上应用diff1，得到branch1
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>diff1");
		diff1.forEach(d -> {
			System.out.println(d);
		});
		merger.copyAllLeftToRight(diff1, null);
		ChangeTool.save(baseResource, branch1URI);

		// 恢复原来的base
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>backup");
		backupScope = new DefaultComparisonScope(backupResource, baseResource, null);
		backupComparison = EMFCompare.builder().build().compare(backupScope);
		backupDiffs = backupComparison.getDifferences();
		merger.copyAllLeftToRight(backupDiffs, null);

		// 在base上应用diff2，得到branch2
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>diff2");
		diff2.forEach(d -> {
			System.out.println(d);
		});
		merger.copyAllLeftToRight(diff2, null);
		ChangeTool.save(baseResource, branch2URI);

		// 恢复原来的base
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>backup");
		backupScope = new DefaultComparisonScope(backupResource, baseResource, null);
		backupComparison = EMFCompare.builder().build().compare(backupScope);
		backupDiffs = backupComparison.getDifferences();
		merger.copyAllLeftToRight(backupDiffs, null);

		// 在base上应用diff3，得到branch3
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>diff3");
		diff3.forEach(d -> {
			System.out.println(d);
		});
		merger.copyAllLeftToRight(diff3, null);
		ChangeTool.save(baseResource, branch3URI);

		System.out.println("done");
	}

	public static void testMerge() {

		ArrayList<URI> uriList = new ArrayList<>();
		uriList.add(baseURI);
		uriList.add(branch1URI);
		uriList.add(branch2URI);
		uriList.add(branch3URI);

		TypeGraph typeGraph = EcoreModelUtil.load(ep);

		long start = System.currentTimeMillis();
		NWay nWay = new NWay(NsURIName, ep, typeGraph);
		List<MatchN> matches = nWay.nMatch(uriList);
		// 制定了需要排序的边类型为allTypeEdges
		List<TypeEdge> allTypeEdges = typeGraph.getAllTypeEdges();
		TypedGraph mergeModel = nWay.nMerge(matches, allTypeEdges);
		long end = System.currentTimeMillis();
		System.out.println("总耗时：" + (end - start) + "ms.");
		System.out.println("****************************************************");

		try {
			nWay.saveModel(metaModelURI, m1URI, mergeModel);
		} catch (NothingReturnedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("done");

	}

	// 比较M1和M0
	public static void testEquality() {

		Resource leftResource = resourceSet.getResource(m1URI, true);
		Resource rightResource = resourceSet.getResource(m0URI, true);

		IComparisonScope scope = new DefaultComparisonScope(leftResource, rightResource, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);

		EList<Diff> diffs = comparison.getDifferences();
		System.out.println("diffs.size(): " + diffs.size());
		diffs.forEach(d -> {
			System.out.println(d);
		});

	}

}
