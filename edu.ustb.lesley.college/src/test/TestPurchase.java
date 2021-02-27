package test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;

import edu.ustb.sei.mde.bxcore.exceptions.NothingReturnedException;
import edu.ustb.sei.mde.graph.type.TypeGraph;
import edu.ustb.sei.mde.graph.typedGraph.TypedGraph;
import my.MatchN;
import nway.EcoreTypeGraph;
import nway.NWay;

public class TestPurchase {

	public static void main(String[] args) {

		String NsURI = EcorePackage.eNS_URI;
		EcorePackage ep = EcorePackage.eINSTANCE;

		URI uriBase = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/purchase.xmi");
		URI uriBranch1 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/purchase1.xmi");
		URI uriBranch2 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/purchase2.xmi");
		URI uriBranch3 = URI.createFileURI("E:/git/n-way/edu.ustb.lesley.college/src/test/purchase3.xmi");

		ArrayList<URI> uriList = new ArrayList<>();
		uriList.add(uriBase);
		uriList.add(uriBranch1);
		uriList.add(uriBranch2);
//		uriList.add(uriBranch3);

		EcoreTypeGraph et = new EcoreTypeGraph();
		TypeGraph typeGraph = et.getTypeGraph_Ecore();

		String metaModelPath = "E:\\git\\n-way\\edu.ustb.lesley.compare\\model\\Ecore.ecore";
		String mergeModelPath = "E:/git/n-way/edu.ustb.lesley.college/src/test/purchase_merge.xmi";

		NWay nWay = new NWay(NsURI, ep, uriList, typeGraph, metaModelPath, mergeModelPath);
		List<MatchN> matches = nWay.nMatch();		
		TypedGraph mergeModel = nWay.nMerge(matches, "EClass-*->EStructuralFeature");
				
		try {
			nWay.saveModel(URI.createFileURI(mergeModelPath), mergeModel);
			System.out.println("down");
		} catch (NothingReturnedException e) {
			e.printStackTrace();
		}

	}

}
