package nway;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class ChangeTool {

	
	public static void changeForEcore(Resource resource) {

		ArrayList<EObject> dList = new ArrayList<>();
		EObject rContent = resource.getContents().get(0);
		Random random = new Random();

		TreeIterator<EObject> eAllContents = rContent.eAllContents();
		eAllContents.forEachRemaining(e -> {
			// 给定一个概率
			double nextDouble = random.nextDouble();
			if (nextDouble >= 0.6) {
				if (e instanceof EClass) {	// 修改类的名称
					EClass c = (EClass) e;
					c.setName(c.getName() + "_change");
					System.out.println(c.getName());
				} else if (e instanceof EDataType) {
					// do nothing
				} else if (e instanceof EAttribute) {	// 删除某些属性
					dList.add(e);
					System.out.println(e);
				} else if (e instanceof EReference) {	// 修改引用的名称
					EReference r = (EReference) e;
					r.setName(r.getName() + "_change");
					System.out.println(r.getName());
				} else if (e instanceof EOperation) {	// 删除某些方法
					dList.add(e);
					System.out.println(e);
				}
			}
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		});

		// 调用EcoreUtil的方法进行删除
		EcoreUtil.removeAll(dList);

	}

	public static void changeForXMI(Resource resource) {

		EObject rContent = resource.getContents().get(0);
		Random random = new Random();
		ArrayList<EObject> dList = new ArrayList<>();

		TreeIterator<EObject> eAllContents = rContent.eAllContents();
		eAllContents.forEachRemaining(e -> {
			EClass eClass = e.eClass();			

			// 保证“之后可能被删除的对象（不作为container），不被修改”
			int size = eClass.getEAllContainments().size();

			// 修改属性值
			eClass.getEAllAttributes().forEach(a -> {
				// 给定一个概率
				if (size != 0 && random.nextDouble() >= 0.8) {
					String instanceTypeName = a.getEAttributeType().getInstanceTypeName();
					if (instanceTypeName.contains("String")) {
						String eGet = (String) e.eGet(a);
						e.eSet(a, eGet + "_change");
					} else if (instanceTypeName.contains("int") || instanceTypeName.contains("Int")) {
						Integer eGet = (Integer) e.eGet(a);
						e.eSet(a, 99);
					}
				}

			});

			// 删除不作为container的对象
			// 给定一个概率
			if (size == 0 && random.nextDouble() >= 0.9) {
				System.out.println(e);
				dList.add(e);
			}
			
//			// 新加怎么做？
//			if(random.nextDouble() >= 0.7) {				
//				EObject eContainer = e.eContainer();
//				EObject create = EcoreUtil.create(eClass);
//				System.out.println(create);
//				EcoreUtil.
//			}
			
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>");
		});

		// 调用EcoreUtil的方法进行删除
		EcoreUtil.removeAll(dList);
	

	}
	
	
	

	// 保存为xmi
	public static void save(Resource resource, URI out) {
		try {
			resource.setURI(out);
			resource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
