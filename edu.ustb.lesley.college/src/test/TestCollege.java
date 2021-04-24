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
import org.eclipse.emf.ecore.EPackage;
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
		backupResource = resourceSet.getResource(backupURI, true);
		
		uriList.add(baseURI);
		uriList.add(branch1URI);
		uriList.add(branch2URI);
		uriList.add(branch3URI);

//		getM0();
//		getBranches();
//		testMerge();
//		testEquality(m0URI, m1URI);
		
//		testEMFCompare();
		testEquality(m0URI, m2URI);

	}

	public static void getM0() {

		// 调用自动修改方法
		ChangeTool.changeForXMI(baseResource, m0URI);
		System.out.println("done");
	}

	public static void getBranches() {

		Random random = new Random();

		// 利用EMF Compare比较得到m0与base之间的diffs
		Resource m0Resource = resourceSet.getResource(m0URI, true);
		IComparisonScope scope = new DefaultComparisonScope(baseResource, m0Resource, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		EList<Diff> diffs = comparison.getDifferences();
		
		// tmp
		diffs.forEach(d -> {
			System.out.println(d);
		});

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

		// 将collections中的group随机分配给分支版本
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

		// 将other中的diff随机分配给分支版本
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
		applyDiff(diff1, branch1URI);
		// 恢复原来的base
		backup();

		// 在base上应用diff2，得到branch2
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>diff2");
		applyDiff(diff2, branch2URI);
		// 恢复原来的base
		backup();

		// 在base上应用diff3，得到branch3
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>diff3");
		applyDiff(diff3, branch3URI);

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
		
		// 第一次leftResource为第一个分支
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
