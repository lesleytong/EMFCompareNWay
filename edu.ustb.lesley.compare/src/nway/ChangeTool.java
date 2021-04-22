package nway;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class ChangeTool {

	public static void changeForEcore(Resource resource) {

		ArrayList<EObject> dList = new ArrayList<>();
		EPackage rContent = (EPackage) resource.getContents().get(0);
		Random random = new Random();

		rContent.eAllContents().forEachRemaining(e -> {

			if (e instanceof EClass) {
				EClass c = (EClass) e;
				// 修改类的名称
				if (random.nextDouble() >= 0.8) {
					c.setName(c.getName() + "_" + RandomStringUtils.randomAlphanumeric(3));
					System.out.println("修改后类的名称为：" + c.getName());
				}
				// 新加属性，如果还要指定类型的话，之前需要缓存下EDataType
				if (random.nextDouble() >= 0.8) {
					EAttribute createEAttribute = EcoreFactory.eINSTANCE.createEAttribute();
					createEAttribute.setName(RandomStringUtils.randomAlphanumeric(3));
					c.getEStructuralFeatures().add(createEAttribute);
					System.out.println("新加的属性名：" + createEAttribute.getName());
				}
				// 新加方法
				if (random.nextDouble() >= 0.8) {
					EOperation createEOperation = EcoreFactory.eINSTANCE.createEOperation();
					createEOperation.setName(RandomStringUtils.randomAlphanumeric(3));
					c.getEOperations().add(createEOperation);
					System.out.println("新加的方法名：" + createEOperation.getName());
				}
			}

			else if (e instanceof EAttribute) {
				// 删除某些属性
				if (random.nextDouble() >= 0.8) {
					dList.add(e);
					System.out.println("删除的属性为：" + e);
				} else if (random.nextDouble() >= 0.7) {
					// 修改某些属性
					EAttribute a = (EAttribute) e;
					a.setName(a.getName() + "_" + RandomStringUtils.randomAlphanumeric(3));
					System.out.println("修改后的属性名称为：" + a.getName());
				}
			}

			else if (e instanceof EReference) {
				// 修改关联的名称
				if (random.nextDouble() >= 0.8) {
					EReference r = (EReference) e;
					r.setName(r.getName() + "_" + RandomStringUtils.randomAlphanumeric(3));
					System.out.println("修改后关联的名称为：" + r.getName());
				}
			}

			else if (e instanceof EOperation) {
				// 删除某些方法
				if (random.nextDouble() >= 0.8) {
					dList.add(e);
					System.out.println("删除的方法为：" + e);
				}
			}

			// 新加类
			if (random.nextDouble() >= 0.9) {
				EClass createEClass = EcoreFactory.eINSTANCE.createEClass();
				createEClass.setName(RandomStringUtils.randomAlphanumeric(3));
				rContent.getEClassifiers().add(createEClass);
				System.out.println("新加的类：" + createEClass);
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
		Map<EObject, Collection<EObject>> addMap = new HashMap<>();
		


		rContent.eAllContents().forEachRemaining(e -> {

			EClass eClass = e.eClass();

			// 删除不作为container的对象
			int size = eClass.getEAllContainments().size();
			if (size == 0 && random.nextDouble() >= 0.8) {
				System.out.println("删除的对象" + e);
				dList.add(e);

			}

			if (!dList.contains(e)) {

				// 修改属性值，给定一个概率
				eClass.getEAllAttributes().forEach(a -> {
					if (random.nextDouble() >= 0.8) {
						setAttribute(e, a);
						System.out.println("修改属性值后：" + e.eGet(a));
					}
				});

				// 新加当前对象“一对多关联”的子对象，给定一个概率
				eClass.getEAllContainments().forEach(r -> {
					if (r.isMany() && random.nextDouble() >= 0.9) {
						EClass eReferenceType = r.getEReferenceType(); // 关联的另一方
						EObject create = EcoreUtil.create(eReferenceType);
						eReferenceType.getEAllAttributes().forEach(a -> {
							setAttribute(create, a);
						});
						Collection<EObject> targets = (Collection<EObject>) e.eGet(r);
						addMap.put(create, targets);
						System.out.println("新加的对象：" + create);
					}
				});
				
//				// 新加当前对象“一对多关联”的引用对象，给定一个概率
//				eClass.eCrossReferences().forEach(r -> {
//					
//				});
				
				// 由于root不能被遍历到，单独写root对象下新加”一对多关联“的子对象，给定一个概率
				rContent.eClass().getEAllContainments().forEach(r -> {
					if (r.isMany() && random.nextDouble() >= 0.95) {
						EClass eReferenceType = r.getEReferenceType();
						EObject create = EcoreUtil.create(eReferenceType);
						eReferenceType.getEAllAttributes().forEach(a -> {
							setAttribute(create, a);
						});
						Collection<EObject> targets = (Collection<EObject>) rContent.eGet(r);
						addMap.put(create, targets);
						System.out.println("新加的对象：" + create);
					}
				});
				
			}

			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>");
		});

		// 调用EcoreUtil的方法统一删除
		EcoreUtil.removeAll(dList);

		// 最后再统一新加
		addMap.forEach((key, value) -> {
			value.add(key);
		});
		
	}

	public static void setAttribute(EObject e, EAttribute a) {
		
		if(!a.isMany()) {	// 如果a是单值属性
			String instanceTypeName = a.getEAttributeType().getInstanceTypeName();
			if (instanceTypeName.contains("String")) {
				Object eGet = e.eGet(a);
				if (eGet != null) {
					e.eSet(a, eGet + "_" + RandomStringUtils.randomAlphanumeric(3));
				} else {
					e.eSet(a, RandomStringUtils.randomAlphanumeric(3));
				}
			} else if (instanceTypeName.contains("int") || instanceTypeName.contains("Int")) {
				Random random = new Random();
				int nextInt = 18 + random.nextInt(20);
				e.eSet(a, nextInt);
			}
		}
		
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
