package nway;

import org.eclipse.emf.common.util.BasicMonitor;
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
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class ThreeWayCompare {

	String NsURI = null;
	EPackageImpl packageImpl = null;
	URI baseURI = null;
	URI leftURI = null;
	URI rightURI = null;



	public ThreeWayCompare(String NsURI, EPackageImpl packageImpl, URI baseURI, URI leftURI, URI rightURI) {
		this.NsURI = NsURI;
		this.packageImpl = packageImpl;
		this.baseURI = baseURI;
		this.leftURI = leftURI;
		this.rightURI = rightURI;
	}

	public void threeWay() {

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(NsURI, packageImpl);

		Resource baseResource = resourceSet.getResource(baseURI, true);
		Resource leftResource = resourceSet.getResource(leftURI, true);
		Resource rightResource = resourceSet.getResource(rightURI, true);

		long start = System.currentTimeMillis();
		IComparisonScope scope = new DefaultComparisonScope(leftResource, rightResource, baseResource);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		final EList<Diff> differences = comparison.getDifferences();
		IBatchMerger merger = new BatchMerger(IMerger.RegistryImpl.createStandaloneInstance());
		merger.copyAllLeftToRight(differences, new BasicMonitor());
		long end = System.currentTimeMillis();
		System.out.println("the whole cost time: " + (end - start) + " ms");
		
	}



}
