package pretreat;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import nway.ChangeTool;

public class PreTreatEcore {

	public static void main(String[] args) {

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());

		URI originURI = URI
				.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\pretreat\\benchmark_origin.ecore");
		URI baseURI = URI.createFileURI("E:\\git\\n-way\\edu.ustb.lesley.college\\src\\pretreat\\benchmark.ecore");

		Resource originResource = resourceSet.getResource(originURI, true);

		pretreat(originResource, baseURI);

	}

	public static void pretreat(Resource resource, URI out) {

		resource.getContents().forEach(content -> {
			Set<EClassifier> eTypeSet = new HashSet<>();
			content.eAllContents().forEachRemaining(c -> {
				if (c instanceof EAttribute) {
					EClassifier eType = ((EAttribute) c).getEType();
					eTypeSet.add(eType);
				}
			});
			if (content instanceof EPackage) {
				((EPackage) content).getEClassifiers().addAll(eTypeSet);
			}
		});

		ChangeTool.save(resource, out);

		System.out.println("done");
	}

}
