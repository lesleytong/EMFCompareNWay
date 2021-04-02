package nway;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.resource.Resource;

public class ChangeTool {
	
	public static void change(Resource resource) {
		
		EList<EObject> contents = resource.getContents();
		
		for(int i=0; i<contents.size(); i++) {
			EObject content = contents.get(i);
			if(content instanceof EPackage) {
				EPackage ep = (EPackageImpl)content;
				EList<EObject> epContents = ep.eContents();
				epContents.forEach(e -> {
					System.out.println(e);
					if(e instanceof EClass) {
						EClass ec = (EClassImpl)e;
						EList<EStructuralFeature> eAllStructuralFeatures = ec.getEAllStructuralFeatures();
						eAllStructuralFeatures.forEach(a -> {							
							if(a instanceof EAttribute) {
								EAttribute attr = (EAttributeImpl)a;
								System.out.println(attr.getName());
								
								// 修改属性名、删除一些属性
								
							}
							
						});
					}
					System.out.println("***************************************");
				});
			}
		}
	
		// tmp
		System.out.println("ChangeTool line 25");
		
	}
}
